package com.profileservice.modules.profile.service.impl;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.profileservice.modules.profile.dto.request.UpdateProfileRequest;
import com.profileservice.modules.profile.dto.request.UserEducationRequest;
import com.profileservice.modules.profile.dto.request.UserWorkplaceRequest;
import com.profileservice.modules.profile.dto.response.UserWorkplaceResponse;
import com.profileservice.modules.profile.entity.profile.entity.UserEducation;
import com.profileservice.modules.profile.entity.profile.entity.UserWorkplace;
import com.profileservice.modules.profile.mapper.UserWorkplaceMapper;
import com.profileservice.modules.profile.mapstruct.WorkplaceMapper;
import com.profileservice.modules.profile.service.IWorkplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WorkplaceService extends ServiceImpl<UserWorkplaceMapper,UserWorkplace> implements IWorkplaceService {
    private final WorkplaceMapper workplaceMapper;
    private final UserWorkplaceMapper userWorkplaceMapper;
    @Override
    @Cached(name = "userWorkplaces:", key = "#userId", cacheType = CacheType.BOTH, expire = 10, timeUnit = TimeUnit.MINUTES)
    public List<UserWorkplaceResponse> getWorkplacesByUserId(String userId) {
        return workplaceMapper.toUserWorkplaceResponse(userWorkplaceMapper.selectList(new LambdaQueryWrapper<UserWorkplace>().eq(UserWorkplace::getUserId,userId)));
    }
    @Override
    @Transactional
    @CacheInvalidate(name = "userWorkplaces:", key = "#userId")
    public void syncWorkplaces(String userId, List<UserWorkplaceRequest> requests) {
        System.out.println(requests);
        if (requests == null || requests.isEmpty()) return;

        List<String> existingIds = this.listObjs(
                new LambdaQueryWrapper<UserWorkplace>()
                        .select(UserWorkplace::getId)
                        .eq(UserWorkplace::getUserId, userId),
                obj -> (String) obj
        );

        // 2. Gom danh sách ID từ Frontend gửi lên
        List<String> requestIds = requests.stream()
                .map(UserWorkplaceRequest::getId)
                .filter(Objects::nonNull).toList();
        System.out.println(requestIds);

        // 3. XÓA: ID có trong DB nhưng Frontend không gửi
        List<String> idsToDelete = existingIds.stream()
                .filter(id -> !requestIds.contains(id))
                .toList();

        if (!idsToDelete.isEmpty()) {
            this.removeBatchByIds(idsToDelete);
        }

        // 4. PHÂN LOẠI UPDATE VÀ INSERT
        List<UserWorkplace> listToUpdate = new ArrayList<>();
        List<UserWorkplace> listToInsert = new ArrayList<>();

        for (UserWorkplaceRequest req : requests) {
            // Dummy ID -> Bỏ qua
            if (req.getCompanyName() == null) {
                continue;
            }

            UserWorkplace entity = workplaceMapper.toEntity(req);
            entity.setUserId(userId);

            // LOGIC MỚI: Check String ID
            // Nếu ID không bắt đầu bằng dấu "-" -> Đây là Snowflake ID cũ -> Cần Update
            if (req.getId() != null && !req.getId().startsWith("-")) {
                listToUpdate.add(entity);
            } else {
                // ID có dấu "-" -> Bản ghi mới tạo từ Frontend
                entity.setId(null); // Gán null để MyBatis Plus (@TableId type ASSIGN_ID) tự sinh chuỗi ID mới
                listToInsert.add(entity);
            }
        }

        // 5. THỰC THI BATCH
        if (!listToUpdate.isEmpty()) {
            this.updateBatchById(listToUpdate);
        }
        if (!listToInsert.isEmpty()) {
            this.saveBatch(listToInsert);
        }
    }
}
