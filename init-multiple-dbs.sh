#!/bin/bash
# Lệnh set -e giúp script dừng ngay lập tức nếu có lỗi xảy ra
set -e

# Chạy lệnh psql bằng user mặc định để tạo các DB trống
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE clyvasync_identity_db;
    CREATE DATABASE clyvasync_profile_db;
    CREATE DATABASE clyvasync_post_db;
EOSQL