package com.kidozh.discuzhub.works;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kidozh.discuzhub.daos.ViewHistoryDao;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.entities.Discuz;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;

public class AutoClearHistoriesWork extends Worker {
    private static final String TAG = AutoClearHistoriesWork.class.getSimpleName();
    private Context context;
    private forumUserBriefInfo userBriefInfo;
    Discuz Discuz;
    public AutoClearHistoriesWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

    }


    @NonNull
    @Override
    public Result doWork() {
        // register notification channel


        ViewHistoryDao dao = ViewHistoryDatabase.getInstance(context)
                .getDao();
        dao.deleteViewHistoriesByLimit();

        return Result.success();
    }



}
