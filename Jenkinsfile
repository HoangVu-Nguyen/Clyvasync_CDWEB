pipeline {
    agent any

    environment {
        DOCKER_HUB_USER = 'clyvasync-microservice'
        DOCKER_HUB_CREDS_ID = 'docker-hub-secret'
        EC2_SSH_CREDS_ID = 'ec2-server-microservice-key'
        EC2_IP = '13.221.88.185'
        EC2_USER = 'ubuntu'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Step 1: Push Configs & Ensure Infra is Up') {
            steps {
                echo "Đang đẩy file cấu hình sang EC2 và khởi động hạ tầng..."
                sshagent(["${env.EC2_SSH_CREDS_ID}"]) {
                    // 1. Đẩy 2 file compose sang EC2 (Ghi đè file cũ nếu có)
                    sh "scp -o StrictHostKeyChecking=no docker-compose.yml docker-compose.prod.yml ${env.EC2_USER}@${env.EC2_IP}:~/Clyvasync_Microservice/"

                    // 2. SSH vào EC2 và khởi động hạ tầng (Postgres, Kafka, Redis, SpiceDB)
                    sh """
                        ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} '
                            cd ~/Clyvasync_Microservice && \
                            docker compose -f docker-compose.yml up -d
                        '
                    """
                }
            }
        }

        stage('Step 2: Detect & Build & Push Apps') {
            steps {
                script {
                    def services = ["discovery-server", "identity-service", "profile-service", "media-service", "notification-service", "gateway-service"]

                    // Sử dụng biến mặc định của Jenkins Git Plugin
                    def changedFiles = ""
                    try {
                        // Lấy danh sách file thay đổi so với commit thành công trước đó
                        changedFiles = sh(script: "git diff --name-only ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT} ${env.GIT_COMMIT}", returnStdout: true).trim()
                    } catch (Exception e) {
                        echo "⚠️ Đây có thể là lần build đầu tiên hoặc không tìm thấy commit cũ. Sẽ tiến hành build toàn bộ hệ thống!"
                    }

                    for (service in services) {
                        // Build nếu có sự thay đổi HOẶC đây là lần chạy đầu tiên (changedFiles rỗng)
                        if (changedFiles.contains(service) || !changedFiles) {
                            echo "🚀 Bắt đầu Build & Push: ${service} (Tag: #${env.IMAGE_TAG})..."

                            // 1. Build Jar cho module cụ thể
                            sh "mvn clean package -pl ${service} -am -DskipTests"

                            // 2. Build & Push Docker Image
                            docker.withRegistry('https://index.docker.io/v1/', "${env.DOCKER_HUB_CREDS_ID}") {
                                def appImage = docker.build("${env.DOCKER_HUB_USER}/${service}", "-f ${service}/Dockerfile .")
                                appImage.push("${env.IMAGE_TAG}")
                                appImage.push("latest") // Push thêm tag latest để dự phòng
                            }

                            // 3. Deploy service vừa build
                            deployApp(service, env.IMAGE_TAG, env.DOCKER_HUB_USER)
                        } else {
                            echo "⏭️ Không có thay đổi tại ${service}. Bỏ qua."
                        }
                    }
                }
            }
        }
    }
}

// Chuyển các biến môi trường vào tham số hàm để script SSH hiểu được
def deployApp(serviceName, imageTag, dockerUser) {
    echo "🚚 Đang triển khai ${serviceName} lên EC2..."
    sshagent(["${env.EC2_SSH_CREDS_ID}"]) {
        sh """
            ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} '
                cd ~/Clyvasync_Microservice && \

                # Truyền biến để file docker-compose.prod.yml nhận diện
                export IMAGE_TAG=${imageTag} && \
                export DOCKER_USERNAME=${dockerUser} && \

                # Pull bản build mới nhất
                docker compose -f docker-compose.prod.yml pull ${serviceName} && \

                # Cập nhật duy nhất container này (Không làm sập các service khác)
                docker compose -f docker-compose.prod.yml up -d --no-deps ${serviceName} && \

                # Dọn dẹp Image cũ để tiết kiệm RAM/Disk
                docker image prune -f
            '
        """
    }
}