package com.recruit.im.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.common.api.ResultCode;
import com.recruit.common.exception.BizException;
import com.recruit.im.dto.ImDtos.SendRequest;
import com.recruit.im.dto.ImDtos.StartRequest;
import com.recruit.im.entity.Conversation;
import com.recruit.im.entity.Message;
import com.recruit.im.mapper.ConversationMapper;
import com.recruit.im.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 在线沟通业务：会话创建、消息收发、未读管理
 */
@Service
public class ImService {

    private final ConversationMapper convMapper;
    private final MessageMapper msgMapper;

    public ImService(ConversationMapper convMapper, MessageMapper msgMapper) {
        this.convMapper = convMapper;
        this.msgMapper = msgMapper;
    }

    /** 求职者点击"立即沟通"：不存在会话则创建 */
    public Conversation start(Long fromId, Integer role, StartRequest req) {
        Conversation existed = findPair(fromId, req.getToId());
        if (existed != null) {
            return existed;
        }
        Conversation conv = new Conversation();
        if (role != null && role == 2) {
            conv.setUserB(fromId);
            conv.setUserA(req.getToId());
        } else {
            conv.setUserA(fromId);
            conv.setUserB(req.getToId());
        }
        conv.setJobId(req.getJobId());
        conv.setJobTitle(req.getJobTitle());
        conv.setLastMsg("");
        conv.setLastTime(LocalDateTime.now());
        conv.setUnreadA(0);
        conv.setUnreadB(0);
        convMapper.insert(conv);
        return conv;
    }

    public List<Conversation> list(Long userId) {
        return convMapper.selectList(new LambdaQueryWrapper<Conversation>()
                .and(w -> w.eq(Conversation::getUserA, userId).or().eq(Conversation::getUserB, userId))
                .orderByDesc(Conversation::getLastTime));
    }

    /** 发送消息，返回会话ID */
    public Long send(Long fromId, Integer role, SendRequest req) {
        Conversation conv;
        if (req.getConvId() != null) {
            conv = convMapper.selectById(req.getConvId());
            if (conv == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
        } else {
            StartRequest sr = new StartRequest();
            sr.setToId(req.getToId());
            sr.setJobId(req.getJobId());
            sr.setJobTitle(req.getJobTitle());
            conv = start(fromId, role, sr);
        }
        if (!fromId.equals(conv.getUserA()) && !fromId.equals(conv.getUserB())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }

        Long toId = fromId.equals(conv.getUserA()) ? conv.getUserB() : conv.getUserA();
        Message msg = new Message();
        msg.setConvId(conv.getId());
        msg.setFromId(fromId);
        msg.setToId(toId);
        msg.setContent(req.getContent());
        msg.setReadFlag(0);
        msgMapper.insert(msg);

        conv.setLastMsg(req.getContent());
        conv.setLastTime(LocalDateTime.now());
        if (fromId.equals(conv.getUserA())) {
            conv.setUnreadB((conv.getUnreadB() == null ? 0 : conv.getUnreadB()) + 1);
        } else {
            conv.setUnreadA((conv.getUnreadA() == null ? 0 : conv.getUnreadA()) + 1);
        }
        convMapper.updateById(conv);
        return conv.getId();
    }

    public List<Message> messages(Long convId, Long userId) {
        Conversation conv = convMapper.selectById(convId);
        if (conv == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (!userId.equals(conv.getUserA()) && !userId.equals(conv.getUserB())) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return msgMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getConvId, convId)
                .orderByAsc(Message::getId)
                .last("LIMIT 300"));
    }

    /** 清空当前用户的未读数 */
    public void read(Long convId, Long userId) {
        Conversation conv = convMapper.selectById(convId);
        if (conv == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (userId.equals(conv.getUserA())) {
            conv.setUnreadA(0);
        } else if (userId.equals(conv.getUserB())) {
            conv.setUnreadB(0);
        }
        convMapper.updateById(conv);
    }

    private Conversation findPair(Long a, Long b) {
        return convMapper.selectOne(new LambdaQueryWrapper<Conversation>()
                .and(w -> w.eq(Conversation::getUserA, a).eq(Conversation::getUserB, b))
                .or(w -> w.eq(Conversation::getUserA, b).eq(Conversation::getUserB, a))
                .last("LIMIT 1"));
    }
}
