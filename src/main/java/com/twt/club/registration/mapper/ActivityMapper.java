package com.twt.club.registration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.twt.club.registration.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    @Update("UPDATE activity SET current_participants = current_participants + #{delta}, "
            + "updated_at = NOW() WHERE id = #{activityId} "
            + "AND current_participants + #{delta} >= 0 "
            + "AND (current_participants + #{delta}) <= max_participants")
    int updateCurrentParticipants(@Param("activityId") Long activityId, @Param("delta") int delta);
}
