package com.commonlibrary.dto.record;

import com.commonlibrary.constant.SpiceDBConstants;
import com.commonlibrary.dto.schema.SpiceSchema;
import org.springframework.util.Assert;

public record SpiceDbRel(
        String resourceType,
        String resourceId,
        String relation,
        String subjectType,
        String subjectId
) {


    public SpiceDbRel {
        Assert.hasText(resourceType, "Resource Type cannot be empty");
        Assert.hasText(resourceId, "Resource ID cannot be empty");
        Assert.hasText(relation, "Relation cannot be empty");
        Assert.hasText(subjectId, "Subject ID cannot be empty");

        // Nếu subjectType null thì mặc định là "user"
        if (subjectType == null || subjectType.isBlank()) {
            subjectType = SpiceSchema.USER;
        }
    }
}