package com.profileservice.modules.photo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.profileservice.modules.photo.entity.profile.entity.UserPhoto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserPhotoMapper extends BaseMapper<UserPhoto> {
    @Select("SELECT photo_type, photo_url FROM user_photos " +
            "WHERE user_id = #{userId} AND is_current = true")
    List<UserPhoto> findCurrentPhotos(@Param("userId") Long userId);
}
