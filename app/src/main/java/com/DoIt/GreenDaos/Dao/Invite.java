package com.DoIt.GreenDaos.Dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity
public class Invite {
    @Id
    private Long id;
    private Boolean hasAccepted;
    private Boolean isSender;
    private String message;
    private Long senderId;
    @ToOne(joinProperty = "senderId")
    private Subjects sender;
    private Long receiverId;
    @ToOne(joinProperty = "receiverId")
    private Subjects receiver;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 869221867)
    private transient InviteDao myDao;
    @Generated(hash = 838960172)
    public Invite(Long id, Boolean hasAccepted, Boolean isSender, String message,
            Long senderId, Long receiverId) {
        this.id = id;
        this.hasAccepted = hasAccepted;
        this.isSender = isSender;
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
    @Generated(hash = 941160332)
    public Invite() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Boolean getHasAccepted() {
        return this.hasAccepted;
    }
    public void setHasAccepted(Boolean hasAccepted) {
        this.hasAccepted = hasAccepted;
    }
    public Boolean getIsSender() {
        return this.isSender;
    }
    public void setIsSender(Boolean isSender) {
        this.isSender = isSender;
    }
    public String getMessage() {
        return this.message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Long getSenderId() {
        return this.senderId;
    }
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    public Long getReceiverId() {
        return this.receiverId;
    }
    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }
    @Generated(hash = 880682693)
    private transient Long sender__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 863773146)
    public Subjects getSender() {
        Long __key = this.senderId;
        if (sender__resolvedKey == null || !sender__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SubjectsDao targetDao = daoSession.getSubjectsDao();
            Subjects senderNew = targetDao.load(__key);
            synchronized (this) {
                sender = senderNew;
                sender__resolvedKey = __key;
            }
        }
        return sender;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 919358137)
    public void setSender(Subjects sender) {
        synchronized (this) {
            this.sender = sender;
            senderId = sender == null ? null : sender.getId();
            sender__resolvedKey = senderId;
        }
    }
    @Generated(hash = 118553546)
    private transient Long receiver__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 348696783)
    public Subjects getReceiver() {
        Long __key = this.receiverId;
        if (receiver__resolvedKey == null || !receiver__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SubjectsDao targetDao = daoSession.getSubjectsDao();
            Subjects receiverNew = targetDao.load(__key);
            synchronized (this) {
                receiver = receiverNew;
                receiver__resolvedKey = __key;
            }
        }
        return receiver;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1254211547)
    public void setReceiver(Subjects receiver) {
        synchronized (this) {
            this.receiver = receiver;
            receiverId = receiver == null ? null : receiver.getId();
            receiver__resolvedKey = receiverId;
        }
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1185142297)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getInviteDao() : null;
    }
}
