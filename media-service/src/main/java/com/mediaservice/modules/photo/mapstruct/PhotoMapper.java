package com.mediaservice.modules.photo.mapstruct;

import com.mediaservice.modules.photo.dto.response.PhotoResponse;
import com.mediaservice.modules.photo.entity.UserPhoto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PhotoMapper {
    PhotoResponse toPhotoResponse(UserPhoto userPhoto);
    UserPhoto toUserPhoto(PhotoResponse photoResponse);
    List<PhotoResponse> toPhotoResponseList(List<UserPhoto> userPhotos);
    List<UserPhoto> toUserPhotoList(List<PhotoResponse> photoResponses);
}
