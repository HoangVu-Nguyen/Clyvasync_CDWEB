pipeline {
    agent any

    // 1. Khai báo Maven (Tên 'Maven 3' phải khớp trong Global Tool Configuration)
    tools {
        maven 'Maven 3'
    }

    environment {
        DOCKER_HUB_USER = 'clyvasync'
        DOCKER_HUB_CREDS_ID = 'docker-hub-credentials'
        EC2_SSH_CREDS_ID = 'ec2-server-microservice-key'
        EC2_IP = '13.221.88.185'
        EC2_USER = 'ubuntu'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Step 1: Push Configs & Ensure Infra is Up') {
            steps {
                echo "Đang xử lý và đẩy file cấu hình sang EC2..."
                sh "chmod +x init-multiple-dbs.sh"

                sshagent(["${env.EC2_SSH_CREDS_ID}"]) {
                    sh """
                        # 1. Đảm bảo thư mục tồn tại và có quyền ghi
                        ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} 'mkdir -p ~/Clyvasync_Microservice'

                        # 2. Gửi các file Compose trước
                        scp -o StrictHostKeyChecking=no docker-compose.yml docker-compose.prod.yml ${env.EC2_USER}@${env.EC2_IP}:~/Clyvasync_Microservice/

                        # 3. Gửi file script vào thư mục HOME (tránh bị khóa) rồi mới chuyển vào project
                        scp -o StrictHostKeyChecking=no init-multiple-dbs.sh ${env.EC2_USER}@${env.EC2_IP}:~/init-multiple-dbs.sh.tmp

                        # 4. SSH vào để thực hiện cập nhật và chạy Infra
                        ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} '
                            cd ~/Clyvasync_Microservice && \

                            # Di chuyển file tạm vào vị trí chính thức (ép ghi đè)
                            mv ~/init-multiple-dbs.sh.tmp ./init-multiple-dbs.sh && \
                            chmod +x init-multiple-dbs.sh && \

                            # Khởi động hạ tầng
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
                    def changedFiles = ""

                    // 2. Logic kiểm tra Git an toàn
                    if (env.GIT_PREVIOUS_SUCCESSFUL_COMMIT && env.GIT_PREVIOUS_SUCCESSFUL_COMMIT != "null") {
                        try {
                            changedFiles = sh(script: "git diff --name-only ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT} ${env.GIT_COMMIT}", returnStdout: true).trim()
                            echo "Các file thay đổi: ${changedFiles}"
                        } catch (Exception e) {
                            echo "⚠️ Lỗi git diff: ${e.message}. Sẽ tiến hành build toàn bộ."
                        }
                    } else {
                        echo "ℹ️ Đây là lần build đầu tiên hoặc chưa có commit thành công trước đó. Chế độ: Build All."
                    }

                    for (service in services) {
                        // Nếu thư mục service có tên trong changedFiles HOẶC lần đầu build (changedFiles rỗng)
                        if (changedFiles.contains(service) || changedFiles == "") {
                            echo "🚀 Building & Pushing: ${service}..."

                            // Chạy Maven thông qua tool đã khai báo ở trên
                            sh "mvn clean package -pl ${service} -am -DskipTests"

                            docker.withRegistry('https://index.docker.io/v1/', "${env.DOCKER_HUB_CREDS_ID}") {
                                def appImage = docker.build("${env.DOCKER_HUB_USER}/${service}", "-f ${service}/Dockerfile .")
                                appImage.push("${env.IMAGE_TAG}")
                                appImage.push("latest")
                            }

                            deployApp(service, env.IMAGE_TAG, env.DOCKER_HUB_USER)
                        } else {
                            echo "⏭️ Skip ${service}: Không có thay đổi."
                        }
                    }
                }
            }
        }
    }
}

def deployApp(serviceName, imageTag, dockerUser) {
    echo "🚚 Deploying ${serviceName}..."
    sshagent(["${env.EC2_SSH_CREDS_ID}"]) {
        sh """
            ssh -o StrictHostKeyChecking=no ${env.EC2_USER}@${env.EC2_IP} '
                cd ~/Clyvasync_Microservice && \
                export IMAGE_TAG=${imageTag} && \
                export DOCKER_USERNAME=${dockerUser} && \
                docker compose -f docker-compose.prod.yml pull ${serviceName} && \
                docker compose -f docker-compose.prod.yml up -d --no-deps ${serviceName} && \
                docker image prune -f
            '
        """
    }
}