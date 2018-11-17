package com.DoIt.GreenDaos.Dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;
import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class Projects {
    @Id
    private Long id;
    private String objectId;
    private Date createdAt;
    private Date updatedAt;
    private String title;
    private Integer number;
    private Integer type;
    private String struct;
    private boolean isSelf;
    private boolean hasDeleted;
    private long senderId;
    @ToOne(joinProperty = "senderId")
    private Subjects Sender;
    @ToMany(referencedJoinProperty = "projectsId")
    private List<ProjectItems> projectItemsList;
    @ToMany(referencedJoinProperty = "projectsId")
    private List<Joins> joinsList;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1388812341)
    private transient ProjectsDao myDao;
    @Generated(hash = 1712594035)
    public Projects(Long id, String objectId, Date createdAt, Date updatedAt,
            String title, Integer number, Integer type, String struct,
            boolean isSelf, boolean hasDeleted, long senderId) {
        this.id = id;
        this.objectId = objectId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.title = title;
        this.number = number;
        this.type = type;
        this.struct = struct;
        this.isSelf = isSelf;
        this.hasDeleted = hasDeleted;
        this.senderId = senderId;
    }
    @Generated(hash = 1005158188)
    public Projects() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getObjectId() {
        return this.objectId;
    }
    public void setObjectId(String objectId) {
        this.objectId = objectId;
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
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Integer getNumber() {
        return this.number;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
    public Integer getType() {
        return this.type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public String getStruct() {
        return this.struct;
    }
    public void setStruct(String struct) {
        this.struct = struct;
    }
    public boolean getIsSelf() {
        return this.isSelf;
    }
    public void setIsSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }
    public boolean getHasDeleted() {
        return this.hasDeleted;
    }
    public void setHasDeleted(boolean hasDeleted) {
        this.hasDeleted = hasDeleted;
    }
    public long getSenderId() {
        return this.senderId;
    }
    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }
    @Generated(hash = 1667105234)
    private transient Long Sender__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1442871561)
    public Subjects getSender() {
        long __key = this.senderId;
        if (Sender__resolvedKey == null || !Sender__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SubjectsDao targetDao = daoSession.getSubjectsDao();
            Subjects SenderNew = targetDao.load(__key);
            synchronized (this) {
                Sender = SenderNew;
                Sender__resolvedKey = __key;
            }
        }
        return Sender;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 642458768)
    public void setSender(@NotNull Subjects Sender) {
        if (Sender == null) {
            throw new DaoException(
                    "To-one property 'senderId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.Sender = Sender;
            senderId = Sender.getId();
            Sender__resolvedKey = senderId;
        }
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 146269421)
    public List<ProjectItems> getProjectItemsList() {
        if (projectItemsList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ProjectItemsDao targetDao = daoSession.getProjectItemsDao();
            List<ProjectItems> projectItemsListNew = targetDao
                    ._queryProjects_ProjectItemsList(id);
            synchronized (this) {
                if (projectItemsList == null) {
                    projectItemsList = projectItemsListNew;
                }
            }
        }
        return projectItemsList;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1275103970)
    public synchronized void resetProjectItemsList() {
        projectItemsList = null;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 130721467)
    public List<Joins> getJoinsList() {
        if (joinsList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            JoinsDao targetDao = daoSession.getJoinsDao();
            List<Joins> joinsListNew = targetDao._queryProjects_JoinsList(id);
            synchronized (this) {
                if (joinsList == null) {
                    joinsList = joinsListNew;
                }
            }
        }
        return joinsList;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 334651223)
    public synchronized void resetJoinsList() {
        joinsList = null;
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
    @Generated(hash = 57894901)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getProjectsDao() : null;
    }
}
