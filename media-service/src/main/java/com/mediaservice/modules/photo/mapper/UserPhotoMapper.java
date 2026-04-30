package com.mediaservice.modules.photo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mediaservice.modules.photo.entity.UserPhoto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserPhotoMapper extends BaseMapper<UserPhoto> {
    @Update("UPDATE user_photos SET status = 'ACTIVE' " +
            "WHERE user_id = #{userId} AND photo_url = #{objectKey} AND photo_type = #{type}")
    int confirmPhotoStatus(@Param("userId") String userId,
                           @Param("objectKey") String objectKey,
                           @Param("type") String type);

    @Update("UPDATE user_photos SET status = 'INACTIVE' " +
            "WHERE user_id = #{userId} AND photo_type = #{type} AND photo_url <> #{objectKey}")
    void deactivateOldPhotos(@Param("userId") String userId,
                             @Param("objectKey") String objectKey,
                             @Param("type") String type);
}
