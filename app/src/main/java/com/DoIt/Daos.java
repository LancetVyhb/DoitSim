package com.DoIt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;

import com.DoIt.GreenDaos.Dao.DaoSession;
import com.DoIt.GreenDaos.Dao.Joins;
import com.DoIt.GreenDaos.Dao.JoinsDao;
import com.DoIt.GreenDaos.Dao.ProjectItems;
import com.DoIt.GreenDaos.Dao.ProjectItemsDao;
import com.DoIt.GreenDaos.Dao.Projects;
import com.DoIt.GreenDaos.Dao.ProjectsDao;
import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.GreenDaos.Dao.SubjectsDao;
import com.DoIt.GreenDaos.Dao.Tabs;
import com.DoIt.GreenDaos.Dao.TabsDao;
import com.DoIt.JavaBean.Join;
import com.DoIt.JavaBean.Project;
import com.DoIt.JavaBean.ProjectItem;
import com.DoIt.JavaBean.Subject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Daos {
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formatter =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DaoSession daoSession;
    private static Daos instance;

    public void setDaoSession(DaoSession daoSession){
        this.daoSession = daoSession;//Application初始化时以用户的ObjectId为名创建数据库
    }

    public DaoSession getDaoSession(){
        return daoSession;
    }

    public static Daos getInt(Context context) {
        if (instance != null) return instance;
        else {
            synchronized (Daos.class) {
                instance = new Daos();
                instance.setDaoSession(((DoItApplication) context.getApplicationContext()).getDaoSession());
                return instance;
            }
        }
    }

    public static Daos getInt(Activity activity) {
        if (instance != null) return instance;
        else {
            synchronized (Daos.class) {
                instance = new Daos();
                instance.setDaoSession(((DoItApplication) activity.getApplication()).getDaoSession());
                return instance;
            }
        }
    }

    public static Daos getInt(Service service) {
        if (instance != null) return instance;
        else {
            synchronized (Daos.class) {
                instance = new Daos();
                instance.setDaoSession(((DoItApplication) service.getApplication()).getDaoSession());
                return instance;
            }
        }
    }

    public static Daos getInt(Application application) {
        if (instance != null) return instance;
        else {
            synchronized (Daos.class) {
                instance = new Daos();
                instance.setDaoSession(((DoItApplication) application).getDaoSession());
                return instance;
            }
        }
    }

    static void init(Application application) {
        instance = new Daos();
        instance.setDaoSession(((DoItApplication)application).getDaoSession());
    }
    /**
     * 获取当前用户的subject
     */
    public Subjects getSelf(){
        return daoSession.getSubjectsDao().queryBuilder().
                where(SubjectsDao.Properties.Tab.eq("我")).unique();
    }
    /**
     * 获取所有的订阅频道
     */
    public List<String> getAllChannels(){
        List<String> channels = new ArrayList<>();
        List<Joins> joinsList = getListeningJoinList();
        if (joinsList != null)
            for (Joins joins : joinsList)
                channels.add(joins.getProjects().getObjectId());
        channels.add(getSelf().getObjectId());
        return channels;
    }
    /**
     * 存储获得的subject
     * @param subject 这个subject是由数据监听所得的join\project\projectItem中附带得来的
     * @param tab 标签，默认为“联系人”
     */
    public long setSubjectToDao(JSONObject subject, String tab, boolean isShow) {
        try {
            Subjects subjects = new Subjects();
            //新创建的subject没有updateAt，用createdAt替代
            subjects.setCreatedAt(formatter.parse(subject.optString("createdAt")));
            if (!subject.optString("updatedAt").equals(""))
                subjects.setUpdatedAt(formatter.parse(subject.optString("updatedAt")));
            else subjects.setUpdatedAt(formatter.parse(subject.optString("createdAt")));
            //设置相关属性
            subjects.setIsShow(isShow);
            subjects.setType(subject.optInt("type"));
            subjects.setObjectId(subject.optString("objectId"));
            subjects.setUserName(subject.optString("userName"));
            if (!subject.optString("phoneNumber").equals(""))
                subjects.setPhoneNumber(subject.optString("phoneNumber"));
            if (!subject.optString("headImage").equals(""))
                subjects.setHeadImage(subject.optString("headImage"));
            //如果设置了新的、从未设置过的标签，则需要调用方法存储新的标签
            if (checkTabExits(tab) == null)
                setNewTabToDao(tab);
            subjects.setTab(tab);
            return daoSession.insert(subjects);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 存储获得的subject
     * @param subject 这个subject是由主动向数据库查询得来的或join、project、projectItem附带的来的
     * @param tab 标签，默认为“联系人”
     */
    public long setSubjectToDao(Subject subject, String tab, boolean isShow) {
        try {
            Subjects subjects = new Subjects();
            subjects.setObjectId(subject.getObjectId());
            //新创建的subject没有updateAt，用createdAt替代
            subjects.setCreatedAt(formatter.parse(subject.getCreatedAt()));
            if (subject.getUpdatedAt() != null)
                subjects.setUpdatedAt(formatter.parse(subject.getUpdatedAt()));
            else subjects.setUpdatedAt(formatter.parse(subject.getCreatedAt()));
            //设置相关属性
            subjects.setIsShow(isShow);
            subjects.setType(subject.getType());
            subjects.setUserName(subject.getUserName());
            if (subject.getPhoneNumber() != null)
                subjects.setPhoneNumber(subject.getPhoneNumber());
            if (subject.getHeadImage() != null)
                subjects.setHeadImage(subject.getHeadImage());
            if (checkTabExits(tab) == null)
                setNewTabToDao(tab);
            subjects.setTab(tab);
            return daoSession.insert(subjects);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 更新获得的subject
     * @param subject 这个subject是由数据监听所得的join\project\projectItem中附带得来的
     */
    public long updateSubjectToDao(JSONObject subject){
        try {
            Subjects subjects = checkSubjectsExist(subject.optString("objectId"));
            //更新相关属性
            subjects.setUpdatedAt(formatter.parse(subject.optString("updatedAt")));
            subjects.setUserName(subject.getString("userName"));
            if (!subject.optString("phoneNumber").equals(""))
                subjects.setPhoneNumber(subject.optString("phoneNumber"));
            if(!subject.optString("headImage").equals(""))
                subjects.setHeadImage(subject.optString("headImage"));
            daoSession.update(subjects);
            return subjects.getId();
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 更新获得的subject
     * @param subject 这个subject是由主动向数据库查询得来的的join\project\projectItem中附带得来的
     */
    public long updateSubjectToDao(Subject subject) {
        try {
            Subjects subjects = checkSubjectsExist(subject.getObjectId());
            //更新相关属性
            if (subject.getUserName() != null)
                subjects.setUserName(subject.getUserName());
            if (subject.getPhoneNumber() != null)
                subjects.setPhoneNumber(subject.getPhoneNumber());
            if (subject.getHeadImage() != null)
                subjects.setHeadImage(subject.getHeadImage());
            if (subject.getUpdatedAt() != null)
                subjects.setUpdatedAt(formatter.parse(subject.getUpdatedAt()));
            daoSession.update(subjects);
            return subjects.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 显示或隐藏subjects
     * @param subjects subjects
     */
    public void setSubjectShow(Subjects subjects, boolean isShow){
        subjects.setIsShow(isShow);
        daoSession.getSubjectsDao().update(subjects);
    }
    /**
     * 显示或隐藏subjects
     * @param subjectIdList subjects的id
     */
    public void setSubjectShow(long[] subjectIdList, boolean isShow){
        if (subjectIdList != null) {
            for (long id : subjectIdList) {
                Subjects subjects = getSubjects(id);
                setSubjectShow(subjects, isShow);
            }
        }
    }
    /**
     * 根据用户名查询subjects
     * @param query query
     */
    public List<Subjects> getSubjectListByQuery(String query){
        return daoSession.getSubjectsDao().queryBuilder().
                where(SubjectsDao.Properties.UserName.like("%"+query+"%"),
                        SubjectsDao.Properties.IsShow.eq(true)).list();
    }
    /**
     * 获取除了“我”以外的所有标签，用于展示通讯录列表
     */
    public List<Tabs> getAllTabWithoutSelf(){
        return daoSession.getTabsDao().queryBuilder()
                .where(TabsDao.Properties.Name.notEq("我"))
                .orderCustom(TabsDao.Properties.Id,"DESC")
                .list();
    }
    /**
     * 根据name获取相应的tabs
     * @param name name
     */
    public Tabs checkTabExits(String name){
        return daoSession.getTabsDao().queryBuilder().
                where(TabsDao.Properties.Name.eq(name)).unique();
    }
    /**
     * 更新某一subjects的标签
     * @param subjectsId subjectsId
     * @param tab 标签
     */
    public void updateSubjectsTab(long subjectsId, String tab){
        if(checkTabExits(tab) == null) setNewTabToDao(tab);
        Subjects subjects = getSubjects(subjectsId);
        subjects.setTab(tab);
        daoSession.update(subjects);
    }
    /**
     * 更新一组subjects的标签
     * @param subjectsId subjectsId
     * @param tab 标签
     */
    public void updateSubjectsListTab(long[] subjectsId, String tab){
        if(checkTabExits(tab) == null) setNewTabToDao(tab);
        for (long anSubjectsId : subjectsId) {
            updateSubjectsTab(anSubjectsId, tab);
        }
    }
    /**
     * 获取该标签下所有的subjects
     * @param tab tab
     */
    public List<Subjects> getSubjectListByTab(String tab){
        return daoSession.getSubjectsDao().queryBuilder().
                where(SubjectsDao.Properties.Tab.eq(tab),
                        SubjectsDao.Properties.IsShow.eq(true))
                .orderCustom(SubjectsDao.Properties.Id, "DESC")
                .list();
    }
    /**
     * 存储新的标签
     * @param name name
     */
    public long setNewTabToDao(String name){
        Tabs tabs = new Tabs();
        tabs.setName(name);
        return daoSession.insert(tabs);
    }
    /**
     * 更新标签及其对应的subjects
     * @param list<Subjects> 标签所对应的subjects
     * @param oldName 旧标签
     * @param newName 新标签
     */
    public void updateTabToDao(List<Subjects> list, String oldName, String newName){
        //“联系人”标签为默认标签，不可更改
        if(oldName.equals("联系人")) setNewTabToDao(newName);
        else{//若新标签不存在，则更新旧标签
            if(checkTabExits(newName)==null) {
                Tabs tabs = daoSession.getTabsDao().queryBuilder().
                        where(TabsDao.Properties.Name.eq(oldName)).list().get(0);
                tabs.setName(newName);
                daoSession.update(tabs);
            }
        }
        //连带更新avater的标签
        for (int i=0;i<list.size();i++)
            updateSubjectsTab(list.get(i).getId(),newName);
    }
    /**
     * 删除标签，更新对应的subjects的标签
     * @param list<Subjects> 标签所对应的subjects
     * @param oldName 旧标签
     */
    public void deleteTab(List<Subjects> list,String oldName){
        Tabs tabs = daoSession.getTabsDao().queryBuilder().
                where(TabsDao.Properties.Name.eq(oldName)).list().get(0);
        //删除标签
        daoSession.getTabsDao().deleteByKey(tabs.getId());
        //更新对应的avaters的标签
        for (int i=0;i<list.size();i++)
            updateSubjectsTab(list.get(i).getId(),"联系人");
    }
    /**
     * 存储获得的Join，以及所携带的project和joiner
     * @param join JSONObject意味着这个Join是由数据监听得来的
     */
    public long setJoinToDao(JSONObject join) {
        try {
            JSONObject project = join.optJSONObject("project");
            JSONObject joiner = join.optJSONObject("joiner");
            Joins joins = new Joins();
            Projects projects;
            Subjects joiners;
            //检查join对应的project是否已存储，是则提取出来，否则存储该project
            if ((projects = checkProjectsExist(project.optString("objectId"))) == null)
                projects = getProjects(setProjectToDao(project));
            //检查join对应的joiner是否已存储，是则提取出来，否则存储该joiner
            if ((joiners = checkSubjectsExist(joiner.optString("objectId"))) == null)
                joiners = getSubjects(setSubjectToDao(joiner, "联系人", true));
            else updateSubjectToDao(joiner);
            //存储相关属性
            joins.setCreatedAt(formatter.parse(join.optString("createdAt")));
            //新创建的join没有updateAt，用createdAt替代
            if (!join.optString("updatedAt").equals(""))
                joins.setUpdatedAt(formatter.parse(join.optString("updatedAt")));
            else joins.setUpdatedAt(formatter.parse(join.optString("createdAt")));
            joins.setObjectId(join.optString("objectId"));
            joins.setPrivacy(join.optInt("privacy"));
            joins.setRole(join.optInt("role"));
            joins.setHasDeleted(false);
            //检查该join的joiner是否为自己
            if (joiners == getSelf()) {
                if (getNowJoinList().size() == 0) joins.setClickTime(0);
                else joins.setClickTime(getNowJoinList().get(0).getClickTime() + 1);
                joins.setIsSelf(true);
                joins.setImportance(0);//紧要性标记
                joins.setNewItem(0);//新消息数量
                joins.setStatus(0);//任务状态标记
                joins.setWorkedAt(new Date());
            } else joins.setIsSelf(false);
            //存储关联entity
            joins.setProjects(projects);
            joins.setJoiner(joiners);
            joins.setId(daoSession.insert(joins));
            projects.resetJoinsList();
            return joins.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 存储获得的Join，以及所携带的project和sender
     * @param join 这个Join是由主动向服务器查询获得的
     */
    public long setJoinToDao(Join join) {
        try {
            Joins joins = new Joins();
            Projects projects;
            Subjects joiners;
            //检查join对应的project是否已存储，是则提取出来，否则存储该project
            if ((projects = checkProjectsExist(join.getProject().getObjectId())) == null)
                projects = getProjects(setProjectToDao(join.getProject()));
            //检查join对应的joiner是否已存储，是则提取出来，否则存储该joiner
            if ((joiners = checkSubjectsExist(join.getJoiner().getObjectId())) == null)
                joiners = getSubjects(setSubjectToDao(join.getJoiner(), "联系人", false));
            else updateSubjectToDao(join.getJoiner());
            //存储相关属性
            joins.setCreatedAt(formatter.parse(join.getCreatedAt()));
            //新创建的join没有updateAt，用createdAt替代
            if (join.getUpdatedAt() != null)
                joins.setUpdatedAt(formatter.parse(join.getUpdatedAt()));
            else joins.setUpdatedAt(formatter.parse(join.getCreatedAt()));
            joins.setObjectId(join.getObjectId());
            joins.setRole(join.getRole());
            joins.setPrivacy(join.getPrivacy());
            joins.setHasDeleted(false);
            //检查该join的joiner是否为自己
            if (joiners == getSelf()) {
                if (getNowJoinList().size() == 0) joins.setClickTime(0);
                else joins.setClickTime(getNowJoinList().get(0).getClickTime() + 1);
                joins.setIsSelf(true);
                joins.setImportance(0);//紧要性标记
                joins.setNewItem(1);//新消息数量
                joins.setStatus(0);//任务状态标记
                joins.setWorkedAt(new Date());
            } else joins.setIsSelf(false);
            //存储关联entity
            joins.setProjects(projects);
            joins.setJoiner(joiners);
            joins.setId(daoSession.insert(joins));
            projects.resetJoinsList();
            return joins.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 更新获得的Join
     * @param join 这个Join是由数据监听得来的
     */
    public long updateJoinToDao(JSONObject join) {
        try {
            Joins joins = checkJoinsExist(join.optString("objectId"));
            //如果该join与当前用户有关，则需要设置新消息
            if (joins.getIsSelf()) joins.setNewItem(1);
            //更新相关属性
            joins.setUpdatedAt(formatter.parse(join.optString("updatedAt")));
            joins.setRole(join.optInt("role"));
            joins.setPrivacy(join.optInt("privacy"));
            joins.update();
            joins.resetProjectItemsList();
            return joins.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 更新获得的Join
     * @param join 这个Join是主动向数据库查询获得的
     */
    public long updateJoinToDao(Join join) {
        try {
            Joins joins = checkJoinsExist(join.getObjectId());//获取数据库中对应的joins
            //更新相关联的project
            updateProjectToDao(join.getProject());
            //如果该join与当前用户有关，则需要设置新消息
            if (joins.getIsSelf()) joins.setNewItem(1);
            //更新相关属性
            joins.setUpdatedAt(formatter.parse(join.getUpdatedAt()));
            joins.setRole(join.getRole());
            joins.setPrivacy(join.getPrivacy());
            joins.update();
            joins.resetProjectItemsList();
            return joins.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 处理废弃Joins
     * @param objectId objectId
     * @param isDeleteProject 此次删除只是删除这个join还是删除整个project过程中的一部分
     */
    public long deleteJoin(String objectId, boolean isDeleteProject) {
        try {
            Joins joins = checkJoinsExist(objectId);//获取数据库中对应的joins
            //处理join对应的projectItem
            List<ProjectItems> list = joins.getProjectItemsList();
            for (int i = 0; i < list.size(); i++)
                deleteProjectItem(list.get(i).getObjectId(), isDeleteProject);
            //如果join与当前用户无关则直接删除，否则设置新消息通知等待用户处理
            if (joins.getIsSelf()) {
                joins.setHasDeleted(true);//设置废弃标记
                joins.setNewItem(1);
                joins.update();
                return joins.getId();
            } else {
                Projects projects = joins.getProjects();
                daoSession.getJoinsDao().deleteByKey(joins.getId());
                projects.resetJoinsList();
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 删除当前用户自己的任务
     * @param joins 要删除的自己的joins
     */
    public void deleteSelfJoin(Joins joins){
        Projects projects = joins.getProjects();
        List<ProjectItems> itemsList = joins.getProjectItemsList();
        daoSession.getProjectsDao().deleteByKey(projects.getId());
        daoSession.getJoinsDao().deleteByKey(joins.getId());
        for (ProjectItems projectItems : itemsList)
            daoSession.getProjectItemsDao().deleteByKey(projectItems.getId());
    }
    /**
     * 存储任务集合
     * @param joinList 任务集合
     */
    public void setOrUpdateJoinListToDao(JSONArray joinList) {
        if (joinList != null && joinList.length() != 0) {
            for (int i = 0; i < joinList.length(); i++) {
                JSONObject join = joinList.optJSONObject(i);
                Joins joins = checkJoinsExist(join.optString("objectId"));
                if (joins == null) setJoinToDao(join);
                else updateJoinToDao(join);
            }
        }
    }
    /**
     * 设置joins的紧要性标记
     * @param importance 紧要性标记
     * @param joinsId joinsId
     */
    public void updateJoinsImportance(long joinsId,int importance){
        Joins joins = getJoins(joinsId);
        joins.setImportance(importance);
        if (importance == 0 && getNowJoinList().size() != 0) {
            joins.setClickTime(getNowJoinList().get(0).getClickTime() + 1);
            joins.setWorkedAt(new Date());
        }
        joins.update();
    }
    /**
     * 设置joins的新消息
     * @param newItem newItem
     * @param joinsId joinsId
     */
    public void setJoinNewItem(long joinsId, int newItem){
        Joins joins = getJoins(joinsId);
        joins.setNewItem(newItem);
        joins.update();
    }
    /**
     * 获取所有joins的新消息数量
     */
    public int getJoinNewItem(){
        List<Joins> joinsList = getNowJoinList();
        int newItemSum = 0;
        if (joinsList != null)
            for (Joins joins : joinsList)
                newItemSum = newItemSum + joins.getNewItem();
        return newItemSum;
    }
    /**
     * 添加joins的新消息
     * @param newItem newItem
     * @param joinsId joinsId
     */
    public void updateJoinsNewItem(long joinsId, int newItem){
        Joins joins = getJoins(joinsId);
        joins.setNewItem(joins.getNewItem()+newItem);
        joins.update();
    }
    /**
     * 根据projectItems来计算对应的joins的任务状态
     * @param projectItems projectItems
     */
    public int checkJoinStatus(ProjectItems projectItems) {
        int status = 0;//计划
        if (projectItems.getOption() == 1) {
            if (projectItems.getAgree() < projectItems.getReject())
                status = 3;//警告
            if (projectItems.getAgree().equals(projectItems.getReject()))
                status = 1;//执行
            if (projectItems.getAgree() > projectItems.getReject())
                status = 2;//完成
        }
        return status;
    }
    /**
     * 根据target的children来计算对应的joins的任务状态
     * @param projectItems projectItems
     */
    public int checkJoinStatusWithTarget(ProjectItems projectItems) {
        int status = 0;//计划
        List<ProjectItems> children = projectItems.getChildren();
        //当还有参与者未同意时，任务状态为计划
        if (projectItems.getAgree().equals(projectItems.getTotal())) {
            for (int i = 0; i < children.size(); i++) {
                if (checkJoinStatus(children.get(i)) != 2) {
                    status = 1;//执行
                    break;//当还有参与者未完成任务时，任务状态为执行
                }//当所有参与者完成任务时，任务状态为完成
                if (i == (children.size() - 1)) status = 2;//完成
            }
        }
        return status;
    }
    /**
     * 根据projectItems来更新对应的joins的任务状态
     * @param joins 所要更新的joins
     */
    public void setJoinStatus(Joins joins) {
        if (joins.getRole() == 0)
            joins.setStatus(checkJoinStatusWithTarget(joins.getProjectItemsList().get(0)));
        else joins.setStatus(checkJoinStatus(joins.getProjectItemsList().get(0)));
        joins.update();
    }
    /**
     * 根据projectItems来更新对应的joins的任务状态
     */
    public void setAllJoinStatus() {
        List<Joins> joinsList = getNowJoinList();
        if (joinsList != null) {
            for (Joins joins : joinsList) {
                joins.resetProjectItemsList();
                setJoinStatus(joins);
                setJoinNewItem(joins.getId(), joins.getProjects().getProjectItemsList().size());
            }
        }
    }
    /**
     * 更新joins的被点击次数
     * @param joins 要更新的joins
     */
    public void addJoinClickTime(Joins joins){
        joins.setClickTime(joins.getClickTime() + 1);
        joins.setWorkedAt(new Date());
        joins.update();
    }
    /**
     * 重置所有的joins的被点击次数，防止数值膨胀
     */
    public void resetJoinClickTime(){
        List<Joins> list = getNowJoinList();
        if (list != null && list.size() != 0) {
            int lessClick = list.get(list.size() - 1).getClickTime();
            for (Joins joins : list) {
                joins.setClickTime(joins.getClickTime() - lessClick);
                joins.update();
            }
        }
    }
    /**
     * 一些许久没有获得新消息或没有被操作的joins会自动被归档
     */
    public void checkJoinsWorkedTime(){
        List<Joins> list = getNowJoinList();
        if (list != null) {
            for (Joins joins : list) {
                Calendar c = Calendar.getInstance();
                c.setTime(joins.getWorkedAt());
                c.add(Calendar.DAY_OF_MONTH, 7);// +7天
                if (new Date().after(c.getTime())) updateJoinsImportance(joins.getId(), 1);
            }
        }
    }
    /**
     * 获取显示在首屏的任务列表
     */
    public List<Joins> getNowJoinList(){
        return  daoSession.getJoinsDao().queryBuilder().where(
                JoinsDao.Properties.IsSelf.eq(true),//排除不是自己的
                JoinsDao.Properties.Importance.eq(0))//排除被归档的
                //按照点击次数降序排序
                .orderCustom(JoinsDao.Properties.ClickTime,"DESC")
                //按照新消息数量降序排序
                .orderCustom(JoinsDao.Properties.NewItem,"DESC")
                .list();
    }
    /**
     * 获取进行数据监听与数据查询的任务列表
     */
    public List<Joins> getListeningJoinList(){
        return daoSession.getJoinsDao().queryBuilder().where(
                JoinsDao.Properties.IsSelf.eq(true),//排除不是自己的
                JoinsDao.Properties.Importance.eq(0),//排除被归档的
                JoinsDao.Properties.HasDeleted.eq(false))//排除已删除或退出的
                .orderCustom(JoinsDao.Properties.UpdatedAt,"DESC")//排序
                .list();
    }
    /**
     * 获取履历
     */
    public List<Joins> getRecordJoinList(){
        return daoSession.getJoinsDao().queryBuilder().where(
                JoinsDao.Properties.IsSelf.eq(true),//排除不是自己的
                JoinsDao.Properties.Importance.eq(1),//排除未被归档的
                JoinsDao.Properties.HasDeleted.eq(false))//排除已删除或退出的
                .orderCustom(JoinsDao.Properties.UpdatedAt,"DESC")
                .list();
    }
    /**
     * 获取可以向某人发出邀请的任务
     * @param subjectsId 其他人的Id
     * @param query 查询语句
     */
    public List<Joins> getInviteOtherJoinList(String query, long subjectsId) {
        List<Joins> list;
        if (!query.equals("")) list = getJoinListByQuery(query);
        else list = getListeningJoinList();
        Iterator<Joins> iterator = list.iterator();
        while (iterator.hasNext()) {
            Joins joins = iterator.next();
            Projects projects = joins.getProjects();
            //排除已归档的和不在运行状态的任务、无法邀请他人权限的任务
            JSONObject power;
            try {
                power = new JSONObject(projects.getStruct());
                if ((!power.optBoolean("isFreeJoin") && joins.getRole() == 2)
                        || projects.getNumber() == 100)
                    iterator.remove();
                else  if (daoSession.getJoinsDao().queryBuilder().where(
                        JoinsDao.Properties.ProjectsId.eq(projects.getId()),
                        JoinsDao.Properties.JoinerId.eq(subjectsId),
                        JoinsDao.Properties.HasDeleted.eq(false))
                        .unique() != null)
                    iterator.remove();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
    /**
     * 获取可以设置地点的任务
     * @param query 查询语句
     */
    public List<Joins> getSetPlaceJoinList(String query){
        List<Joins> list;
        if (!query.equals("")) list = getJoinListByQuery(query);
        else list = getListeningJoinList();
        Iterator<Joins> iterator = list.iterator();
        while (iterator.hasNext()) {
            Joins joins = iterator.next();
            Projects projects = joins.getProjects();
            if (joins.getRole() != 0 || projects.getNumber() == 100)
                iterator.remove();
        }
        return list;
    }
    /**
     * 获取子任务
     * @param query 查询语句
     */
    public List<Joins> getSetContentJoinList(String query){
        List<Joins> list = new ArrayList<>();
        List<Projects> projects;
        if (!query.equals(""))
            projects = daoSession.getProjectsDao().queryBuilder()
                    .where(ProjectsDao.Properties.Title.like("%" + query + "%"),
                            ProjectsDao.Properties.HasDeleted.eq(false))
                    .orderCustom(ProjectsDao.Properties.UpdatedAt, "DESC")
                    .list();
        else projects = daoSession.getProjectsDao().queryBuilder()
                .where(ProjectsDao.Properties.HasDeleted.eq(false))
                .orderCustom(ProjectsDao.Properties.UpdatedAt, "DESC")
                .list();
        for (Projects projects1 : projects) {
            Joins joins = new Joins();
            joins.setProjects(projects1);
            list.add(joins);
        }
        return list;
    }
    /**
     * 根据projects的标题查询joins
     * @param query query
     */
    public List<Joins> getJoinListByQuery(String query) {
        List<Projects> projects = daoSession.getProjectsDao().queryBuilder()
                .where(ProjectsDao.Properties.Title.like("%" + query + "%"),
                        ProjectsDao.Properties.HasDeleted.eq(false))
                .orderCustom(ProjectsDao.Properties.UpdatedAt, "DESC")
                .list();
        List<Joins> joins = new ArrayList<>();
        for (Projects project : projects) {
            Joins join;
            if ((join = getJoinByProject(project.getObjectId())) != null)
                if (join.getIsSelf()
                        && !join.getHasDeleted()
                        && join.getImportance() == 0)
                    joins.add(join);
        }
        return joins;
    }
    /**
     * 根据projects的标题查询已归档的joins
     * @param query query
     */
    public List<Joins> getRecordJoinListByQuery(String query){
        List<Projects> projects = daoSession.getProjectsDao().queryBuilder()
                .where(ProjectsDao.Properties.Title.like("%" + query + "%"),
                        ProjectsDao.Properties.HasDeleted.eq(false))
                .orderCustom(ProjectsDao.Properties.UpdatedAt, "DESC")
                .list();
        List<Joins> joins = new ArrayList<>();
        for (Projects project : projects) {
            Joins join;
            if ((join = getJoinByProject(project.getObjectId())) != null)
                if (join.getIsSelf()
                        && !join.getHasDeleted()
                        && join.getImportance() == 1)
                    joins.add(join);
        }
        return joins;
    }
    /**
     * 根据joins获取对应的projects
     * @param projectId projectId
     */
    public Joins getJoinByProject(long projectId){
        return daoSession.getJoinsDao().queryBuilder().
                where(JoinsDao.Properties.ProjectsId.eq(projectId),
                        JoinsDao.Properties.IsSelf.eq(true)).unique();
    }
    /**
     * 根据joins获取对应的projects
     * @param projectObjectId projectObjectId
     */
    public Joins getJoinByProject(String projectObjectId) {
        Projects projects = checkProjectsExist(projectObjectId);
        if (projects != null)
            return daoSession.getJoinsDao().queryBuilder().where
                    (JoinsDao.Properties.ProjectsId.eq(projects.getId()),
                            JoinsDao.Properties.IsSelf.eq(true)).unique();
        else return null;
    }
    /**
     * 存储获得的project，以及所携带的sender
     * @param project 这个project是由数据监听所得的join中附带得来的
     */
    public long setProjectToDao(JSONObject project) {
        try {
            JSONObject sender = project.optJSONObject("sender");
            //检查对应的sender是否已存储，是则提取出来，否则调用相应方法存储该sender
            Subjects senders;
            if ((senders = checkSubjectsExist(sender.optString("objectId"))) == null)
                senders = getSubjects(setSubjectToDao(sender, "联系人", true));
            else updateSubjectToDao(sender);
            //存储相关属性
            Projects projects = new Projects();
            projects.setSender(senders);
            projects.setIsSelf(false);
            projects.setHasDeleted(false);
            projects.setType(project.optInt("type"));
            projects.setTitle(project.optString("title"));
            projects.setObjectId(project.optString("objectId"));
            projects.setNumber(project.optInt("number"));
            projects.setStruct(project.optString("struct"));
            projects.setCreatedAt(formatter.parse(project.optString("createdAt")));
            //新创建的project没有updateAt，用createdAt替代
            if (!project.optString("updatedAt").equals(""))
                projects.setUpdatedAt(formatter.parse(project.optString("updatedAt")));
            else projects.setUpdatedAt(formatter.parse(project.optString("createdAt")));
            return daoSession.getProjectsDao().insert(projects);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 存储获得的project，以及所携带的sender
     * @param project 这个project是主动向数据库查询获得join中附带得来的
     */
    public long setProjectToDao(Project project) {
        try {
            Subject sender = project.getSender();
            //检查对应的sender是否已存储，是则提取出来，否则调用相应方法存储该sender
            Subjects senders;
            if ((senders = checkSubjectsExist(sender.getObjectId())) == null)
                senders = getSubjects(setSubjectToDao(sender, "联系人", true));
            else updateSubjectToDao(project.getSender());
            //存储相关属性
            Projects projects = new Projects();
            projects.setSender(senders);
            projects.setHasDeleted(false);
            projects.setType(project.getType());
            projects.setTitle(project.getTitle());
            projects.setObjectId(project.getObjectId());
            projects.setNumber(project.getNumber());
            projects.setStruct(project.getStruct());
            //新创建的project没有updateAt，用createdAt替代
            projects.setCreatedAt(formatter.parse(project.getCreatedAt()));
            if (project.getUpdatedAt() != null)
                projects.setUpdatedAt(formatter.parse(project.getUpdatedAt()));
            else projects.setUpdatedAt(formatter.parse(project.getCreatedAt()));
            if (project.getSender().getObjectId().equals(getSelf().getObjectId()))
                projects.setIsSelf(true);
            else projects.setIsSelf(false);
            return daoSession.getProjectsDao().insert(projects);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 更新获得的project
     * @param project 这个project是由数据监听得来的
     */
    public long updateProjectToDao(JSONObject project) {
        try {
            Projects projects = checkProjectsExist(project.optString("objectId"));
            //更新对应的与当前用户相关的joins新消息通知
            Joins joins;
            if ((joins = getJoinByProject(projects.getId())) != null)
                setJoinNewItem(joins.getId(), 1);
            //更新相关属性
            projects.setUpdatedAt(formatter.parse(project.optString("updatedAt")));
            projects.setStruct(project.optString("struct"));
            projects.setNumber(project.optInt("number"));
            projects.setTitle(project.optString("title"));
            projects.update();
            projects.resetJoinsList();
            return projects.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 更新获得的project
     * @param project 这个project是主动向数据库查询获得join中附带得来的
     */
    public long updateProjectToDao(Project project) {
        try {
            Projects projects = checkProjectsExist(project.getObjectId());
            //更新对应的与当前用户相关的joins新消息通知
            Joins joins;
            if ((joins = getJoinByProject(projects.getId())) != null)
                setJoinNewItem(joins.getId(), 1);
            projects.setUpdatedAt(formatter.parse(project.getUpdatedAt()));
            projects.setStruct(project.getStruct());
            projects.setNumber(project.getNumber());
            projects.setTitle(project.getTitle());
            projects.update();
            projects.resetJoinsList();
            return projects.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 处理废弃projects
     * @param objectId objectId
     */
    public long deleteProject(String objectId) {
        try {
            Projects projects = checkProjectsExist(objectId);
            //处理相关连的joins及对应的projectItems
            List<Joins> joinsList = projects.getJoinsList();
            for (Joins joins : joinsList)
                deleteJoin(joins.getObjectId(), true);
            projects.setHasDeleted(true);//设置废弃标志
            projects.update();
            return projects.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 任务参加人数减一
     * @param objectId project的objectId
     */
    public void minusProjectNumber(String objectId){
        Projects projects = checkProjectsExist(objectId);
        projects.setNumber(projects.getNumber() - 1);
        projects.update();
    }
    /**
     * 检查某个任务是否正在监听
     * @param objectId project的objectId
     */
    public boolean checkProjectIsListening(String objectId){
        List<Joins> list = getListeningJoinList();
        List<String> listeningProjectList = new ArrayList<>();
        for (Joins joins : list)
            listeningProjectList.add(joins.getProjects().getObjectId());
        return listeningProjectList.contains(objectId);
    }
    /**
     * 存储获得的projectItem，以及所携带的sender、project、parent
     * @param projectItem 这个projectItem是由数据监听得来的
     */
    public long setProjectItemToDao(JSONObject projectItem) {
        try {
            ProjectItems projectItems = new ProjectItems();
            ProjectItems parent = null;
            Projects projects;
            Joins senders;
            //检查对应的sender是否已存储，是则提取出来，否则调用相应    方法存储该sender
            JSONObject sender = projectItem.optJSONObject("sender");
            if ((senders = checkJoinsExist(sender.optString("objectId"))) == null)
                senders = getJoins(setJoinToDao(sender));
                //检查对应的sender是否与当前用户有关，是则设置该projectItem与当前用户有关
            if (senders.getJoiner() == getSelf()) projectItems.setIsSelf(true);
            else {
                projectItems.setIsSelf(false);
                updateSubjectToDao(sender.optJSONObject("joiner"));
            }
            //获取对应的projects，该projects肯定已存储
            projects = checkProjectsExist
                    (projectItem.optJSONObject("project").optString("objectId"));
            //若该projectItem的type不为0，则需要对他的parent进行处理
            if (projectItem.optInt("type") != 0) {
                parent = checkProjectItemsExist
                        (projectItem.optJSONObject("parent").optString("objectId"));
                projectItems.setParent(parent);
                //如果该projectItem与当前用户相关，则需要对parent设置已回复标记
                if (projectItems.getIsSelf())
                    updateProjectItemHasReplied(true, parent);
                if (parent.getIsSelf()) projectItems.setBeReplied(true);
                //更新parent的回复状态属性
                addNewProjectItemStatus(parent, projectItem.getInt("option"));
            }
            //设置相关属性
            projectItems.setCreatedAt(formatter.parse(projectItem.optString("createdAt")));
            //新创建的projectItem没有updateAt，用createdAt替代
            if (!projectItem.optString("updatedAt").equals(""))
                projectItems.setUpdatedAt(formatter.parse(projectItem.optString("updatedAt")));
            else projectItems.setUpdatedAt(formatter.parse(projectItem.optString("createdAt")));
            projectItems.setObjectId(projectItem.optString("objectId"));
            projectItems.setContent(projectItem.optString("content"));
            projectItems.setOption(projectItem.optInt("option"));
            projectItems.setType(projectItem.optInt("type"));
            //设置回复状态属性
            projectItems.setTotal(0);//回复总人数
            projectItems.setAgree(0);//赞成人数
            projectItems.setReject(0);//反对人数
            //设置关联entity
            projectItems.setSender(senders);
            projectItems.setProjects(projects);
            projectItems.setId(daoSession.insert(projectItems));
            senders.resetProjectItemsList();
            projects.resetProjectItemsList();
            if (parent != null) parent.resetChildren();
            return projectItems.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 存储获得的projectItem，以及所携带的sender、project、parent
     * @param projectItem 这个projectItem是由主动向数据库查询得来的
     */
    public long setProjectItemToDao(ProjectItem projectItem) {
        try {
            ProjectItem parent = projectItem.getParent();
            ProjectItems projectItems = new ProjectItems();
            ProjectItems parents = null;
            Projects projects;
            Joins senders;
            Join sender = projectItem.getSender();
            //检查对应的sender是否已存储，是则提取出来，否则调用相应方法存储该sender
            if ((senders = checkJoinsExist(sender.getObjectId())) == null)
                senders = getJoins(setJoinToDao(sender));
            //检查对应的sender是否与当前用户有关，是则设置该projectItem与当前用户有关
            if (senders.getJoiner() == getSelf()) projectItems.setIsSelf(true);
            else {
                projectItems.setIsSelf(false);
                updateSubjectToDao(sender.getJoiner());
            }
            //获取对应的projects，该projects肯定已存储
            projects = checkProjectsExist(projectItem.getProject().getObjectId());
            //若该projectItem的type不为0，则需要对他的parent进行处理
            if (projectItem.getType() != 0) {
                parents = checkProjectItemsExist(parent.getObjectId());
                projectItems.setParent(parents);
                //如果该projectItem与当前用户相关，则需要对parent设置已回复标记
                if (projectItems.getIsSelf())
                    updateProjectItemHasReplied(true, parents);
                if (parents.getIsSelf()) projectItems.setBeReplied(true);
                //更新parent的回复状态属性
                addNewProjectItemStatus(parents, projectItem.getOption());
            }
            //设置相关属性
            projectItems.setObjectId(projectItem.getObjectId());
            projectItems.setCreatedAt(formatter.parse(projectItem.getCreatedAt()));
            //新创建的projectItem没有updateAt，用createdAt替代
            if (projectItem.getUpdatedAt() != null)
                projectItems.setUpdatedAt(formatter.parse(projectItem.getUpdatedAt()));
            else projectItems.setUpdatedAt(formatter.parse(projectItem.getCreatedAt()));
            projectItems.setContent(projectItem.getContent());
            projectItems.setOption(projectItem.getOption());
            projectItems.setType(projectItem.getType());
            //设置回复状态属性
            projectItems.setTotal(0);//回复总人数
            projectItems.setAgree(0);//赞成人数
            projectItems.setReject(0);//反对人数
            //设置关联entity
            projectItems.setSender(senders);
            projectItems.setProjects(projects);
            projectItems.setId(daoSession.insert(projectItems));
            senders.resetProjectItemsList();
            projects.resetProjectItemsList();
            if (parents != null) parents.resetChildren();
            return projectItems.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 更新获得的projectItem
     * @param projectItem 这个projectItem是由数据监听得来的
     */
    public long updateProjectItemToDao(JSONObject projectItem) {
        try {
            ProjectItems projectItems =
                    checkProjectItemsExist(projectItem.optString("objectId"));
            //更新children的option
            List<ProjectItems> children;
            if (projectItems.getType() != 2 && (children = projectItems.getChildren()) != null) {
                resetProjectItemStatus(projectItems);
                updateChildrenOption(children);
            }
            //如果projectItem的option更改，需要更新parent的回复状态属性
            if (projectItems.getType() != 0)
                if (projectItems.getOption() != projectItem.getInt("option"))
                    updateProjectItemStatus(projectItems,
                            projectItem.getInt("option"), projectItems.getOption());
            //更新相关属性
            projectItems.setOption(projectItem.optInt("option"));
            projectItems.setContent(projectItem.optString("content"));
            projectItems.setUpdatedAt
                    (formatter.parse(projectItem.optString("updatedAt")));
            projectItems.update();
            projectItems.resetChildren();
            return projectItems.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 更新获得的projectItem
     * @param projectItem 这个projectItem是主动向数据库查询得来的
     */
    public long updateProjectItemToDao(ProjectItem projectItem) {
        try {
            ProjectItems projectItems =
                    checkProjectItemsExist(projectItem.getObjectId());
            //更新children的option
            List<ProjectItems> children;
            if (projectItems.getType() != 2 &&
                    (children = projectItems.getChildren()) != null) {
                resetProjectItemStatus(projectItems);
                updateChildrenOption(children);
            }
            //如果projectItem的option更改，需要更新parent的回复状态属性
            if (projectItems.getType() != 0)
                if (!projectItems.getOption().equals(projectItem.getOption()))
                    updateProjectItemStatus(projectItems,
                            projectItem.getOption(), projectItems.getOption());
            //更新相关属性
            projectItems.setOption(projectItem.getOption());
            projectItems.setContent(projectItem.getContent());
            projectItems.setUpdatedAt(formatter.parse(projectItem.getUpdatedAt()));
            projectItems.update();
            projectItems.resetChildren();
            return projectItems.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 处理废弃projectItems
     * @param objectId 用于获取要删除的projectItems
     * @param isDeleteParent parent是否也会被删除，会则不用计算回复状态了
     */
    public long deleteProjectItem(String objectId, boolean isDeleteParent) {
        try {
            ProjectItems projectItems = checkProjectItemsExist(objectId);
            ProjectItems parent = new ProjectItems();
            Integer option = projectItems.getOption();
            Boolean isSelf = projectItems.getIsSelf();
            //更新parent的回复状态属性
            if (projectItems.getType() != 0) parent = projectItems.getParent();
            //连带删除children
            if (projectItems.getType() != 2) {
                List<ProjectItems> itemsList = projectItems.getChildren();
                for (ProjectItems items : itemsList)
                    deleteProjectItem(items.getObjectId(), true);
            }
            daoSession.getProjectItemsDao().deleteByKey(projectItems.getId());
            if (!isDeleteParent && parent != null) deleteProjectItemStatus(parent,option,isSelf);
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    /**
     * 存储或更新projectItem
     * @param list 通过向服务器查询得来projectItem
     */
    public void setOrUpdateProjectItemListToDao(List<ProjectItem> list){
        //根据type逐级上升存储或更新projectItem
        for (int i = 0; i < 3; i++) {
            for (ProjectItem item : list) {
                if (item.getType().equals(i)) {
                    //检查对应的projectItem是否存在，是则更新，否则存储
                    if (checkProjectItemsExist(item.getObjectId()) == null)
                        setProjectItemToDao(item);
                    else updateProjectItemToDao(item);
                }
            }
        }
    }
    /**
     * 存储或更新projectItem
     * @param list 通过云函数回调得来projectItem
     */
    public long[] setOrUpdateProjectItemListToDao(JSONArray list){
        //根据type逐级上升存储或更新projectItem
        long id[] = new long[list.length()];
        for (int i = 0; i < 3; i++) {
            for (int o = 0; o < list.length(); o++) {
                JSONObject item = list.optJSONObject(o);
                if (item.optInt("type") == i) {
                    //检查对应的projectItem是否存在，是则更新，否则存储
                    if (checkProjectItemsExist(item.optString("objectId")) == null)
                        id[o] = setProjectItemToDao(item);
                    else id[o] = updateProjectItemToDao(item);
                }
            }
        }
        return id;
    }
    /**
     * 存储通过向服务器查询得来的混杂的projectItem
     * @param list 通过向服务器查询得来的混杂的projectItem
     */
    public void divideProjectItemList(JSONArray list) throws Exception {
        List<String> projectObjectIdList = new ArrayList<>();
        if (list != null) {
            //提取ProjectObjectId
            for (int i = 0; i < list.length(); i++) {
                String projectObjectId = list.optJSONObject(i)
                        .optJSONObject("project").optString("objectId");
                if (!projectObjectIdList.contains(projectObjectId))
                    projectObjectIdList.add(projectObjectId);
            }
            //根据projectObjectId分拆list，根据projectObjectId分别存储projectItem
            for (String objectId : projectObjectIdList) {
                JSONArray items = new JSONArray();
                for (int i = 0; i < list.length(); i++) {
                    String projectObjectId = list.optJSONObject(i)
                            .optJSONObject("project").optString("objectId");
                    if (projectObjectId.equals(objectId)) items.put(list.optJSONObject(i));
                }
                setOrUpdateProjectItemListToDao(items);
            }
        }
    }
    /**
     * 检查监听到的新projectItem是否与当前用户有关系
     * @param id 新projectItem的id
     */
    public boolean checkProjectItemRelative(long id) {
        ProjectItems projectItems = getProjectItems(id);
        return projectItems.getIsSelf() || projectItems.getHasReplied() || projectItems.getBeReplied();
    }
    /**
     * 更新parent的回复设置
     * @param hasReplied 是否已回复
     * @param parent parent
     */
    public void updateProjectItemHasReplied(boolean hasReplied,ProjectItems parent){
        parent.setHasReplied(hasReplied);
        parent.update();
    }
    /**
     * 当parent更新时更新children的option
     * @param items parent对应的children
     */
    public void updateChildrenOption(List<ProjectItems> items){
        for (ProjectItems item : items) {
            item.resetChildren();
            item.setOption(3);
            List<ProjectItems> children;
            if(item.getType() == 1 && ((children = item.getChildren()).size()) != 0) {
                resetProjectItemStatus(item);
                updateChildrenOption(children);
            } else item.update();
        }
    }
    /**
     * projectItems更新后要归零status
     * @param projectItems 要归零的projectItems
     */
    public void resetProjectItemStatus(ProjectItems projectItems){
        projectItems.setAgree(0);
        projectItems.setReject(0);
        projectItems.update();
        projectItems.resetChildren();
    }
    /**
     * 当有一个新的child添加时根据child的option更新parent的回复状态属性
     * @param parent 新的projectItems的parent
     * @param option child的回复
     */
    public void addNewProjectItemStatus(ProjectItems parent,Integer option) {
        //根据child的option更新parent的回复状态属性
        parent.setTotal(parent.getTotal() + 1);
        if (option == 1) parent.setAgree(parent.getAgree() + 1);
        if (option == 2) parent.setReject(parent.getReject() + 1);
        parent.update();
        parent.resetChildren();
    }
    /**
     * 当有一个child更新时根据child的option更新parent的回复状态属性
     * @param projectItems 新的projectItems
     * @param newOption child的旧回复
     * @param oldOption child的新回复
     */
    public void updateProjectItemStatus
    (ProjectItems projectItems,Integer newOption,Integer oldOption) {
        ProjectItems parent = projectItems.getParent();
        //根据child的option的新旧变化更新parent的回复状态属性
        if (newOption == 1) parent.setAgree(parent.getAgree() + 1);
        if (newOption == 2) parent.setReject(parent.getReject() + 1);
        if (oldOption == 1) parent.setAgree(parent.getAgree() - 1);
        if (oldOption == 2) parent.setReject(parent.getReject() - 1);
        parent.update();
        parent.resetChildren();
    }
    /**
     * 当有一个child被删除时根据child的option更新parent的回复状态属性
     * @param parent parent
     * @param oldOption child的option
     * @param isSelf child是否与当前用户相关
     */
    public void deleteProjectItemStatus(ProjectItems parent, Integer oldOption,boolean isSelf) {
        //根据children的option更新parent的回复状态属性
        parent.resetChildren();
        parent.setTotal(parent.getTotal() - 1);
        if (oldOption == 1) parent.setAgree(parent.getAgree() - 1);
        if (oldOption == 2) parent.setReject(parent.getReject() - 1);
        if (isSelf) parent.setHasReplied(false);
        parent.update();
        if (parent.getIsSelf()) setJoinStatus(parent.getSender());
        if (parent.getBeReplied()) setJoinStatus(parent.getParent().getSender());
    }
    /**
     * 根据projectsId获取其对应的target(type==0的projectItems)
     * @param projectsId projectsId
     */
    public ProjectItems getTargetByProjectId(long projectsId){
        return daoSession.getProjectItemsDao().queryBuilder().where(
                ProjectItemsDao.Properties.ProjectsId.eq(projectsId),
                ProjectItemsDao.Properties.Type.eq(0)
        ).unique();
    }
    /**
     * 根据objectId获取subjects
     * @param objectId objectId
     */
    public Subjects checkSubjectsExist(String objectId){
        return daoSession.getSubjectsDao().queryBuilder().where
                (SubjectsDao.Properties.ObjectId.eq(objectId)).unique();
    }
    /**
     * 根据objectId获取projects
     * @param objectId objectId
     */
    public Projects checkProjectsExist(String objectId){
        return daoSession.getProjectsDao().queryBuilder().where
                (ProjectsDao.Properties.ObjectId.eq(objectId)).unique();
    }
    /**
     * 根据objectId获取joins
     * @param objectId objectId
     */
    public Joins checkJoinsExist(String objectId){
        return daoSession.getJoinsDao().queryBuilder().where
                (JoinsDao.Properties.ObjectId.eq(objectId)).unique();
    }
    /**
     * 根据objectId获取prijectItems
     * @param objectId objectId
     */
    public ProjectItems checkProjectItemsExist(String objectId){
        return daoSession.getProjectItemsDao().queryBuilder().where
                (ProjectItemsDao.Properties.ObjectId.eq(objectId)).unique();
    }
    /**
     * 根据id获取joins
     * @param joinsId joinsId
     */
    public Joins getJoins(long joinsId){
        return daoSession.getJoinsDao().queryBuilder().where
                (JoinsDao.Properties.Id.eq(joinsId)).unique();
    }
    /**
     * 根据id获取projects
     * @param projectsId projectsId
     */
    public Projects getProjects(long projectsId){
        return daoSession.getProjectsDao().queryBuilder().
                where(ProjectsDao.Properties.Id.eq(projectsId)).unique();
    }
    /**
     * 根据id获取subjects
     * @param subjectsId subjectsId
     */
    public Subjects getSubjects(long subjectsId){
        return daoSession.getSubjectsDao().queryBuilder().where
                (SubjectsDao.Properties.Id.eq(subjectsId)).unique();
    }
    /**
     * 根据id获取projectItems
     * @param projectItemId projectItemId
     */
    public ProjectItems getProjectItems(long projectItemId){
        return daoSession.getProjectItemsDao().queryBuilder().where
                (ProjectItemsDao.Properties.Id.eq(projectItemId)).unique();
    }
}
