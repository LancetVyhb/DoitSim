package com.DoIt;

import com.DoIt.GreenDaos.Dao.Subjects;
import com.DoIt.JavaBean.AppVersion;
import com.DoIt.JavaBean.Join;
import com.DoIt.JavaBean.Project;
import com.DoIt.JavaBean.ProjectItem;
import com.DoIt.JavaBean.Subject;
import com.tencent.map.geolocation.TencentLocation;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;

import static com.DoIt.View.FirstPage.CHANNEL;

public class Bmobs {
    /**
     * 通过搜索关键字查询用户
     *
     * @param queryText 搜索关键字
     * @param isName    用户通过用户名还是手机号码查询用户
     * @param skip      跳过数
     * @param result    回调方法
     */
    public static void searchSubject
    (String queryText, boolean isName, Subjects self, int skip, Result<List<Subject>> result) {
        List<BmobQuery<Subject>> andList = new ArrayList<>();
        //屏蔽自己
        BmobQuery<Subject> query1 = new BmobQuery<>();
        if (isName) query1.addWhereNotEqualTo("userName", self.getUserName());
        else query1.addWhereNotEqualTo("phoneNumber", self.getPhoneNumber());
        andList.add(query1);

        BmobQuery<Subject> query2 = new BmobQuery<>();
        if (isName) query2.addWhereEqualTo("userName", queryText);
        else query2.addWhereEqualTo("phoneNumber", queryText);
        andList.add(query2);

        BmobQuery<Subject> main = new BmobQuery<>();
        main.and(andList);
        main.setLimit(20);//设置分页
        main.setSkip(skip);//设置跳过条数
        main.findObjects(new FindListener<Subject>() {
            private Result<List<Subject>> result;
            @Override
            public void done(List<Subject> list, BmobException e) {
                result.onData(list, e);
            }
            private FindListener<Subject> setResult(Result<List<Subject>> result) {
                this.result = result;
                return this;
            }
        }.setResult(result));
    }

    /**
     * 初次登陆后导入当前用户的手机通讯录
     *
     * @param numbers 通过用户手机通讯录获得的手机号码列表
     * @param result  回调方法
     */
    public static void getSubjectListByPhoneNumber(List<String> numbers, Result<List<Subject>> result) {
        List<BmobQuery<Subject>> queries = new ArrayList<>();
        for (String number : numbers) {
            BmobQuery<Subject> query = new BmobQuery<>();
            query.addWhereEqualTo("phoneNumber", number);
            queries.add(query);
        }
        BmobQuery<Subject> main = new BmobQuery<>();
        main.or(queries);
        main.findObjects(new FindListener<Subject>() {
            private Result<List<Subject>> result;
            @Override
            public void done(List<Subject> list, BmobException e) {
                result.onData(list, e);
            }
            private FindListener<Subject> setResult(Result<List<Subject>> result) {
                this.result = result;
                return this;
            }
        }.setResult(result));
    }

    /**
     * 每当访问他人时都会调用此方法获取或进行上拉加载更多
     *
     * @param subjectObjectId subject表的objectId
     * @param skip            要跳过的条目
     * @param result          回调方法
     */
    public static void getJoinListBySubjectObjectId
    (String subjectObjectId, int skip, Result<List<Join>> result) {
        List<BmobQuery<Join>> andList = new ArrayList<>();
        Subject subject = new Subject();
        subject.setObjectId(subjectObjectId);
        BmobQuery<Join> query1 = new BmobQuery<>();
        query1.addWhereEqualTo("joiner", new BmobPointer(subject));
        andList.add(query1);

        BmobQuery<Join> query2 = new BmobQuery<>();
        query2.addWhereEqualTo("privacy", 0);
        andList.add(query2);

        BmobQuery<Join> query = new BmobQuery<>();
        query.and(andList);
        query.setLimit(20);//设置分页
        query.setSkip(skip);//设置跳过条数
        query.order("-createdAt");
        query.include("project.sender");
        query.findObjects(new FindListener<Join>() {
            private Result<List<Join>> result;
            @Override
            public void done(List<Join> list, BmobException e) {
                result.onData(list, e);
            }
            private FindListener<Join> setResult(Result<List<Join>> result) {
                this.result = result;
                return this;
            }
        }.setResult(result));
    }

    /**
     * 被拉入到某一任务后获取任务内容
     *
     * @param projectObjectId 任务的objectId
     * @param result          回调方法
     */
    static void getProjectItemListByProject
    (String projectObjectId, Result<List<ProjectItem>> result) {
        Project project = new Project();
        project.setObjectId(projectObjectId);
        BmobQuery<ProjectItem> query = new BmobQuery<>();
        query.addWhereEqualTo("project", new BmobPointer(project));
        query.include("sender.joiner");
        query.findObjects(new FindListener<ProjectItem>() {
            private Result<List<ProjectItem>> result;
            @Override
            public void done(List<ProjectItem> list, BmobException e) {
                result.onData(list, e);
            }
            private FindListener<ProjectItem> setResult(Result<List<ProjectItem>> result) {
                this.result = result;
                return this;
            }
        }.setResult(result));
    }

    /**
     * 每次查看附近的任务时都会调用此方法获取或进行上拉加载更多
     *
     * @param location 当前用户的当前位置
     * @param skip     要跳过的条目
     * @param result   回调方法
     */
    public static void getProjectListByGeoPoint
    (TencentLocation location, int skip, Result<List<Project>> result) {
        List<BmobQuery<Project>> andList = new ArrayList<>();
        //设置距离条件
        BmobQuery<Project> query1 = new BmobQuery<>();
        query1.addWhereWithinKilometers("place",
                new BmobGeoPoint(location.getLongitude(), location.getLatitude()), 100.0);
        andList.add(query1);
        //设置人数条件
        BmobQuery<Project> query2 = new BmobQuery<>();
        query2.addWhereLessThan("number", 100);
        andList.add(query2);

        BmobQuery<Project> main = new BmobQuery<>();
        main.and(andList);
        main.setLimit(20);
        main.setSkip(skip);
        main.order("-createdAt");
        main.include("sender");
        main.findObjects(new FindListener<Project>() {
            private Result<List<Project>> result;
            @Override
            public void done(List<Project> list, BmobException e) {
                result.onData(list, e);
            }
            private FindListener<Project> setResult(Result<List<Project>> result) {
                this.result = result;
                return this;
            }
        }.setResult(result));
    }

    /**
     * 每当访问当前用户没有参加的project时调用该方法获取该project的target
     *
     * @param projectObjectId project表的objectId
     * @param result          回调方法
     */
    public static void getTargetByProject(String projectObjectId, Result<ProjectItem> result) {
        List<BmobQuery<ProjectItem>> and = new ArrayList<>();
        BmobQuery<ProjectItem> query1 = new BmobQuery<>();
        Project project = new Project();
        project.setObjectId(projectObjectId);
        query1.addWhereEqualTo("project", new BmobPointer(project));
        and.add(query1);

        BmobQuery<ProjectItem> query2 = new BmobQuery<>();
        query2.addWhereEqualTo("type", 0);
        and.add(query2);

        BmobQuery<ProjectItem> query = new BmobQuery<>();
        query.include("sender.joiner");
        query.and(and);
        query.findObjects(new FindListener<ProjectItem>() {
            private Result<ProjectItem> result;
            @Override
            public void done(List<ProjectItem> list, BmobException e) {
                result.onData(list.get(0), e);
            }
            private FindListener<ProjectItem> setResult(Result<ProjectItem> result) {
                this.result = result;
                return this;
            }
        }.setResult(result));
    }

    /**
     * 每当访问当前用户没有参加的project并点击projectItem隐藏/显示按钮时调用该方法获取该projectItem
     *
     * @param objectId projectItem表的objectId
     * @param result   回调方法
     */
    public static void updateProjectItem(String objectId, Result<ProjectItem> result) {
        BmobQuery<ProjectItem> query = new BmobQuery<>();
        query.include("sender.joiner");
        query.getObject(objectId, new QueryListener<ProjectItem>() {
            private Result<ProjectItem> result;
            @Override
            public void done(ProjectItem projectItem, BmobException e) {
                result.onData(projectItem, e);
            }
            private QueryListener<ProjectItem> setResult(Result<ProjectItem> result) {
                this.result = result;
                return this;
            }
        }.setResult(result));
    }

    /**
     * 每当访问当前用户没有参加的project并浏览projectItem的children时调用该方法获取该parent的children
     *
     * @param parentObjectId projectItem表的objectId
     * @param result         回调方法
     */
    public static void getProjectItemByParent
    (String parentObjectId, Result<List<ProjectItem>> result) {
        BmobQuery<ProjectItem> query = new BmobQuery<>();
        ProjectItem projectItem = new ProjectItem();
        projectItem.setObjectId(parentObjectId);
        query.addWhereEqualTo("parent", new BmobPointer(projectItem));
        query.include("sender.joiner");
        query.order("-updatedAt");
        query.findObjects(new FindListener<ProjectItem>() {
            private Result<List<ProjectItem>> result;
            @Override
            public void done(List<ProjectItem> list, BmobException e) {
                result.onData(list, e);
            }
            private FindListener<ProjectItem> setResult(Result<List<ProjectItem>> result) {
                this.result = result;
                return this;
            }
        }.setResult(result));
    }

    /**
     * 检查版本更新
     *
     * @param result 回调方法
     */
    public static void checkAppUpdate(Result<AppVersion> result) {
        BmobQuery<AppVersion> query = new BmobQuery<>();
        query.addWhereEqualTo("channel", CHANNEL);
        query.findObjects(new FindListener<AppVersion>() {
            private Result<AppVersion> result;
            @Override
            public void done(List<AppVersion> list, BmobException e) {
                result.onData(list.get(0), e);
            }
            private FindListener<AppVersion> setResult(Result<AppVersion> result) {
                this.result = result;
                return this;
            }
        }.setResult(result));
    }

    /**
     * 回调接口
     */
    public interface Result<T> {
        void onData(T t, BmobException e);
    }
}
