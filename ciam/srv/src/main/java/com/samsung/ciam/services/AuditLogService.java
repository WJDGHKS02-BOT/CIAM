package com.samsung.ciam.services;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.samsung.ciam.models.BtpAuditLog;
import com.samsung.ciam.models.QBtpAuditLog;
import com.samsung.ciam.models.QChannels;
import com.samsung.ciam.models.QUsers;
import com.samsung.ciam.repositories.BtpAuditLogRepository;
import com.samsung.ciam.repositories.ChannelRepository;
import com.samsung.ciam.utils.StringUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AuditLogService {

  private QBtpAuditLog qBtpAuditLog = QBtpAuditLog.btpAuditLog;
  private QUsers qUsers = QUsers.users;
  private QChannels qChannels = QChannels.channels;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private BtpAuditLogRepository btpAuditLogRepository;


  public ModelAndView auditLogList(HttpSession session, Model model) {
    ModelAndView modelAndView = new ModelAndView("myPage");

    try {
      String channel = (String) session.getAttribute("session_channel");

      // common code
      // channel (channels 테이블 > value : channel_name / name : channel_display_name)
      modelAndView.addObject("channel", channelRepository.selectChannelTypeList(""));

      modelAndView.addObject("session_channel", channel);

    } catch (Exception e) {
      log.error("Error auditLogList processing failed", e);
    }

    String menu = "auditLog";
    String content = "fragments/myPage/" + menu;

    modelAndView.addObject("content", content);
    modelAndView.addObject("menu", menu);

    return modelAndView;

  }

  public List<Map<String, Object>> getAuditLogList(JPAQueryFactory jpaQueryFactory, Map<String, String> param, String[] columnNameList) {
    List<Map<String, Object>> btpAuditLogList = new ArrayList<>();
    List<Tuple> queryResultList = jpaQueryFactory.select(this.qBtpAuditLog, this.qUsers.email, this.qChannels.channelDisplayName)
        .from(this.qBtpAuditLog)
        .innerJoin(this.qUsers)
        .on(this.qBtpAuditLog.requesterUid.eq(this.qUsers.cdcUid))
        .innerJoin(this.qChannels)
        .on(this.qBtpAuditLog.channel.equalsIgnoreCase(this.qChannels.channelName))
        .where(
            // Requester UID
            this.getColumnExpression(this.qBtpAuditLog, columnNameList[0], param.get(columnNameList[0])),
            // Channel
            this.getColumnExpression(this.qBtpAuditLog, columnNameList[2], param.get(columnNameList[2])),
            // Menu
            this.getColumnExpression(this.qBtpAuditLog, columnNameList[3], param.get(columnNameList[3])),
            // Menu Type
            this.getColumnExpression(this.qBtpAuditLog, columnNameList[4], param.get(columnNameList[4])),
            // Action
            this.getColumnExpression(this.qBtpAuditLog, columnNameList[5], param.get(columnNameList[5])),
            // Date
            this.getColumnExpression(this.qBtpAuditLog, columnNameList[6], param.get(columnNameList[6]))
        )
        .orderBy(qBtpAuditLog.createdAt.desc(), qBtpAuditLog.updatedAt.desc())
        .fetch();

    for (Tuple data : queryResultList) {
      Map<String, Object> btpAuditLogData = new HashMap<>();
      for (String columName : columnNameList) {
        btpAuditLogData.put(columName, this.getColumnValue(data, columName));
      }
      btpAuditLogData.put("details", this.getColumnValue(data, "details"));
      btpAuditLogData.put("reason", this.getColumnValue(data, "reason"));
      btpAuditLogList.add(btpAuditLogData);
    }

    return btpAuditLogList;
  }

  public void addAuditLog(HttpSession session, Map<String, String> auditLogData) {
    if (auditLogData != null) {
      ZoneId koreaZoneId = ZoneId.of("Asia/Seoul");
      ZonedDateTime koreaTime = ZonedDateTime.now(koreaZoneId);

      // ZonedDateTime을 Timestamp로 변환
      Timestamp currentTimestamp = Timestamp.valueOf(koreaTime.toLocalDateTime());

      //Timestamp currenTimestamp = new Timestamp(System.currentTimeMillis());

      BtpAuditLog auditLog = new BtpAuditLog();
      String uid;
      if (!StringUtil.isEmpty(auditLogData.get("uid"))) {
        uid = (String) auditLogData.get("uid");
      } else {
        if (session != null && session.getAttribute("cdc_uid") != null) {
          uid = (String) session.getAttribute("cdc_uid");
        } else {
          uid = "generated-uuid";
        }
      }

      String channel;
      if (!StringUtil.isEmpty(auditLogData.get("channel"))) {
        channel = (String) auditLogData.get("channel");
      } else {
        if (session != null && session.getAttribute("session_channel") != null) {
          channel = (String) session.getAttribute("session_channel");
        } else {
          channel = "partnerhub";
        }
      }

      auditLog.setRequesterUid(uid);
      auditLog.setChannel(channel);
      auditLog.setType(StringUtil.getStringValue(auditLogData.get("type"), ""));
      auditLog.setMenuType(StringUtil.getStringValue(auditLogData.get("menu_type"), ""));
      auditLog.setAction(StringUtil.getStringValue(auditLogData.get("action"), ""));
      //auditLog.setResultCount(Integer.getInteger(auditLogData.get("result_count"), 0));
      try {
        // String을 int로 변환
        int resultCount = Integer.parseInt(StringUtil.getStringValue(auditLogData.get("result_count"), "0"));

        // resultCount를 auditLog에 설정
        auditLog.setResultCount(resultCount);
      } catch (NumberFormatException e) {
        // 변환 실패 시 기본값 0 설정
        auditLog.setResultCount(0);
      }
      auditLog.setItems(StringUtil.getStringValue(auditLogData.get("items"), ""));
      auditLog.setCondition(StringUtil.getStringValue(auditLogData.get("condition"), ""));
      auditLog.setReason(StringUtil.getStringValue(auditLogData.get("reason"), ""));
      auditLog.setCreatedAt(currentTimestamp);
      auditLog.setUpdatedAt(currentTimestamp);

      btpAuditLogRepository.save(auditLog);
    } else {
      this.addAuditLog();
    }
  }

  // audit log data to test
  private void addAuditLog() {
    BtpAuditLog auditLog = new BtpAuditLog();
    auditLog.setRequesterUid("generated-uuid");
    auditLog.setChannel("partnerhub");
    auditLog.setType("Personal_Information");
    auditLog.setAction("ListView");
    auditLog.setResultCount(5);
    auditLog.setItems("example-item");
    auditLog.setCondition("example-condition-sentence");
    auditLog.setReason("example-reason-text");
    auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    auditLog.setMenuType("");

    btpAuditLogRepository.save(auditLog);
  }

  private BooleanExpression getColumnExpression(QBtpAuditLog qBtpAuditLog, String columnName, String value) {

    if (StringUtil.isEmpty(value)) {
      return null;
    }

    switch (columnName) {
      case "requester_uid":
        return qBtpAuditLog.requesterUid.containsIgnoreCase(value);

      case "channel":
        return qBtpAuditLog.channel.equalsIgnoreCase(value);

      case "type":
        return qBtpAuditLog.type.containsIgnoreCase(value);

      case "menu_type":
        return qBtpAuditLog.menuType.equalsIgnoreCase(value);

      case "action":
        return qBtpAuditLog.action.equalsIgnoreCase(value);

      case "created_at":
        String dateRange[] = value.split(" ~ ");
        Timestamp from = Timestamp.valueOf(dateRange[0] + " 00:00:00");
        Timestamp to = Timestamp.valueOf(dateRange[1] + " 23:59:59");
        return qBtpAuditLog.createdAt.between(from, to);

      default:
        return null;
    }
  }

  private Object getColumnValue(Tuple tuple, String columnId) {
    switch (columnId) {
      case "requester_uid":
        return tuple.get(qBtpAuditLog).getRequesterUid();

      case "email":
        return tuple.get(qUsers.email);

      case "channel":
        return tuple.get(qChannels.channelDisplayName);

      case "type":
        return tuple.get(qBtpAuditLog).getType();

      case "menu_type":
        return tuple.get(qBtpAuditLog).getMenuType();

      case "action":
        return tuple.get(qBtpAuditLog).getAction();

      case "details":
        return tuple.get(qBtpAuditLog).getCondition();

      case "reason":
        return tuple.get(qBtpAuditLog).getReason();

      case "created_at":
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String resultDate;
        if (tuple.get(qBtpAuditLog).getCreatedAt() != null) {
          resultDate = df.format(new Date(tuple.get(qBtpAuditLog).getCreatedAt().getTime()));
        } else {
          resultDate = "";
        }
        return resultDate;

      default:
        return null;
    }

  }
}
