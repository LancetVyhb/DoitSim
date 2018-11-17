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
public class Joins {
    @Id
    private Long id;
    private boolean isSelf;
    private boolean hasDeleted;
    private String objectId;
    private Date createdAt;
    private Date updatedAt;
    private Date workedAt;
    private Integer newItem;
    private Integer clickTime;
    private Integer role;
    private Integer importance;
    private Integer privacy;
    private Integer status;
    private Long joinerId;
    @ToOne(joinProperty = "joinerId")
    private Subjects joiner;
    private long projectsId;
    @ToOne(joinProperty = "projectsId")
    private Projects projects;
    @ToMany(referencedJoinProperty = "senderId")
    private List<ProjectItems> projectItemsList;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 38164083)
    private transient JoinsDao myDao;
    @Generated(hash = 2033196522)
    public Joins(Long id, boolean isSelf, boolean hasDeleted, String objectId,
            Date createdAt, Date updatedAt, Date workedAt, Integer newItem,
            Integer clickTime, Integer role, Integer importance, Integer privacy,
            Integer status, Long joinerId, long projectsId) {
        this.id = id;
        this.isSelf = isSelf;
        this.hasDeleted = hasDeleted;
        this.objectId = objectId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.workedAt = workedAt;
        this.newItem = newItem;
        this.clickTime = clickTime;
        this.role = role;
        this.importance = importance;
        this.privacy = privacy;
        this.status = status;
        this.joinerId = joinerId;
        this.projectsId = projectsId;
    }
    @Generated(hash = 1171827217)
    public Joins() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public Date getWorkedAt() {
        return this.workedAt;
    }
    public void setWorkedAt(Date workedAt) {
        this.workedAt = workedAt;
    }
    public Integer getNewItem() {
        return this.newItem;
    }
    public void setNewItem(Integer newItem) {
        this.newItem = newItem;
    }
    public Integer getClickTime() {
        return this.clickTime;
    }
    public void setClickTime(Integer clickTime) {
        this.clickTime = clickTime;
    }
    public Integer getRole() {
        return this.role;
    }
    public void setRole(Integer role) {
        this.role = role;
    }
    public Integer getImportance() {
        return this.importance;
    }
    public void setImportance(Integer importance) {
        this.importance = importance;
    }
    public Integer getPrivacy() {
        return this.privacy;
    }
    public void setPrivacy(Integer privacy) {
        this.privacy = privacy;
    }
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Long getJoinerId() {
        return this.joinerId;
    }
    public void setJoinerId(Long joinerId) {
        this.joinerId = joinerId;
    }
    public long getProjectsId() {
        return this.projectsId;
    }
    public void setProjectsId(long projectsId) {
        this.projectsId = projectsId;
    }
    @Generated(hash = 1037644189)
    private transient Long joiner__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 2126642873)
    public Subjects getJoiner() {
        Long __key = this.joinerId;
        if (joiner__resolvedKey == null || !joiner__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SubjectsDao targetDao = daoSession.getSubjectsDao();
            Subjects joinerNew = targetDao.load(__key);
            synchronized (this) {
                joiner = joinerNew;
                joiner__resolvedKey = __key;
            }
        }
        return joiner;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 191899215)
    public void setJoiner(Subjects joiner) {
        synchronized (this) {
            this.joiner = joiner;
            joinerId = joiner == null ? null : joiner.getId();
            joiner__resolvedKey = joinerId;
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
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 845765882)
    public List<ProjectItems> getProjectItemsList() {
        if (projectItemsList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ProjectItemsDao targetDao = daoSession.getProjectItemsDao();
            List<ProjectItems> projectItemsListNew = targetDao
                    ._queryJoins_ProjectItemsList(id);
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
    @Generated(hash = 1692289984)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getJoinsDao() : null;
    }
}