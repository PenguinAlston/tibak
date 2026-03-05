package com.qwen.chat.chat.repository;

import com.qwen.chat.chat.entity.ConversationMybatis;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<ConversationMybatis> {
}
