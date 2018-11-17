package com.DoIt.GreenDaos.Dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;
import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class ProjectItems {
    @Id
    private Long id;
    private boolean hasReplied;
    private boolean beReplied;
    private boolean isSelf;
    private String content;
    private Integer type;
    private Integer option;
    private Integer total;
    private Integer agree;
    private Integer reject;
    private Date createdAt;
    private Date updatedAt;
    private String objectId;
    private long senderId;
    @ToOne(joinProperty = "senderId")
    private Joins sender;
    private long projectsId;
    @ToOne(joinProperty = "projectsId")
    private Projects projects;
    private long parentId;
    @ToOne(joinProperty = "parentId")
    private ProjectItems parent;
    @ToMany(referencedJoinProperty = "parentId")
    private List<ProjectItems> children;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1745726080)
    private transient ProjectItemsDao myDao;
    @Generated(hash = 686721329)
    public ProjectItems(Long id, boolean hasReplied, boolean beReplied,
            boolean isSelf, String content, Integer type, Integer option,
            Integer total, Integer agree, Integer reject, Date createdAt,
            Date updatedAt, String objectId, long senderId, long projectsId,
            long parentId) {
        this.id = id;
        this.hasReplied = hasReplied;
        this.beReplied = beReplied;
        this.isSelf = isSelf;
        this.content = content;
        this.type = type;
        this.option = option;
        this.total = total;
        this.agree = agree;
        this.reject = reject;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.objectId = objectId;
        this.senderId = senderId;
        this.projectsId = projectsId;
        this.parentId = parentId;
    }
    @Generated(hash = 93399285)
    public ProjectItems() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public boolean getHasReplied() {
        return this.hasReplied;
    }
    public void setHasReplied(boolean hasReplied) {
        this.hasReplied = hasReplied;
    }
    public boolean getBeReplied() {
        return this.beReplied;
    }
    public void setBeReplied(boolean beReplied) {
        this.beReplied = beReplied;
    }
    public boolean getIsSelf() {
        return this.isSelf;
    }
    public void setIsSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Integer getType() {
        return this.type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Integer getOption() {
        return this.option;
    }
    public void setOption(Integer option) {
        this.option = option;
    }
    public Integer getTotal() {
        return this.total;
    }
    public void setTotal(Integer total) {
        this.total = total;
    }
    public Integer getAgree() {
        return this.agree;
    }
    public void setAgree(Integer agree) {
        this.agree = agree;
    }
    public Integer getReject() {
        return this.reject;
    }
    public void setReject(Integer reject) {
        this.reject = reject;
    }
    public Date getCreatedAt() {
        return this.createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Date getUpdatedAt() {
        return this.updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    public String getObjectId() {
        return this.objectId;
    }
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
    public long getSenderId() {
        return this.senderId;
    }
    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }
    public long getProjectsId() {
        return this.projectsId;
    }
    public void setProjectsId(long projectsId) {
        this.projectsId = projectsId;
    }
    public long getParentId() {
        return this.parentId;
    }
    public void setParentId(long parentId) {
        this.parentId = parentId;
    }
    @Generated(hash = 880682693)
    private transient Long sender__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 539300379)
    public Joins getSender() {
        long __key = this.senderId;
        if (sender__resolvedKey == null || !sender__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            JoinsDao targetDao = daoSession.getJoinsDao();
            Joins senderNew = targetDao.load(__key);
            synchronized (this) {
                sender = senderNew;
                sender__resolvedKey = __key;
            }
        }
        return sender;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1477407591)
    public void setSender(@NotNull Joins sender) {
        if (sender == null) {
            throw new DaoException(
                    "To-one property 'senderId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.sender = sender;
            senderId = sender.getId();
            sender__resolvedKey = senderId;
        }
    }
    @Generated(hash = 282784041)
    private transient Long projects__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1991624626)
    public Projects getProjects() {
        long __key = this.projectsId;
        if (projects__resolvedKey == null || !projects__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ProjectsDao targetDao = daoSession.getProjectsDao();
            Projects projectsNew = targetDao.load(__key);
            synchronized (this) {
                projects = projectsNew;
                projects__resolvedKey = __key;
            }
        }
        return projects;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1543012729)
    public void setProjects(@NotNull Projects projects) {
        if (projects == null) {
            throw new DaoException(
                    "To-one property 'projectsId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.projects = projects;
            projectsId = projects.getId();
            projects__resolvedKey = projectsId;
        }
    }
    @Generated(hash = 1293412156)
    private transient Long parent__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 133975745)
    public ProjectItems getParent() {
        long __key = this.parentId;
        if (parent__resolvedKey == null || !parent__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ProjectItemsDao targetDao = daoSession.getProjectItemsDao();
            ProjectItems parentNew = targetDao.load(__key);
            synchronized (this) {
                parent = parentNew;
                parent__resolvedKey = __key;
            }
        }
        return parent;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1746993589)
    public void setParent(@NotNull ProjectItems parent) {
        if (parent == null) {
            throw new DaoException(
                    "To-one property 'parentId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.parent = parent;
            parentId = parent.getId();
            parent__resolvedKey = parentId;
        }
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 226503203)
    public List<ProjectItems> getChildren() {
        if (children == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ProjectItemsDao targetDao = daoSession.getProjectItemsDao();
            List<ProjectItems> childrenNew = targetDao
                    ._queryProjectItems_Children(id);
            synchronized (this) {
                if (children == null) {
                    children = childrenNew;
                }
            }
        }
        return children;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1590975152)
    public synchronized void resetChildren() {
        children = null;
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
    @Generated(hash = 1535271433)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getProjectItemsDao() : null;
    }

}
