package com.profileservice.modules.profile.service.impl;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.commonlibrary.service.social.SpiceDbService;
import com.profileservice.modules.profile.dto.request.UserEducationRequest;
import com.profileservice.modules.profile.dto.response.UserEducationResponse;
import com.profileservice.modules.profile.entity.profile.entity.UserEducation;
import com.profileservice.modules.profile.mapper.UserEducationMapper;
import com.profileservice.modules.profile.mapstruct.EducationMapper;
import com.profileservice.modules.profile.service.IEducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationService extends ServiceImpl<UserEducationMapper, UserEducation> implements IEducationService {
    private final UserEducationMapper userEducationMapper;
    private final EducationMapper educationMapper;
    private final SpiceDbService spiceDbService;

    @Override
    @Cached(name = "userEducations:", key = "#userId", cacheType = CacheType.BOTH, expire = 10, timeUnit = TimeUnit.MINUTES)
    public List<UserEducationResponse> getEducationsByUserId(String userId) {
        return educationMapper.toUserEducationResponse(userEducationMapper.selectList(new LambdaQueryWrapper<UserEducation>().eq(UserEducation::getUserId,userId)));
    }

    @Override
    @Transactional
    @CacheInvalidate(name = "userEducations:", key = "#userId")
    public void syncEducations(String userId, List<UserEducationRequest> requests) {
        System.out.println(requests);
        if (requests == null || requests.isEmpty()) return;

        List<String> existingIds = this.listObjs(
                new LambdaQueryWrapper<UserEducation>()
                        .select(UserEducation::getId)
                        .eq(UserEducation::getUserId, userId),
                obj -> (String) obj
        );
        System.out.println(existingIds);

        // 2. Gom danh sách ID từ Frontend gửi lên
        List<String> requestIds = requests.stream()
                .map(UserEducationRequest::getId)
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
        List<UserEducation> listToUpdate = new ArrayList<>();
        List<UserEducation> listToInsert = new ArrayList<>();

        for (UserEducationRequest req : requests) {
            // Dummy ID -> Bỏ qua
            if (req.getSchoolName() == null) {
                continue;
            }

            UserEducation entity = educationMapper.toEntity(req);
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
