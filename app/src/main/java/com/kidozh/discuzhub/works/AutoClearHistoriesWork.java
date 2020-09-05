package com.kidozh.discuzhub.works;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.activities.ViewThreadActivity;
import com.kidozh.discuzhub.daos.ViewHistoryDao;
import com.kidozh.discuzhub.daos.forumInformationDao;
import com.kidozh.discuzhub.database.BBSInformationDatabase;
import com.kidozh.discuzhub.database.ViewHistoryDatabase;
import com.kidozh.discuzhub.database.forumUserBriefInfoDatabase;
import com.kidozh.discuzhub.entities.bbsInformation;
import com.kidozh.discuzhub.entities.forumUserBriefInfo;
import com.kidozh.discuzhub.results.UserNoteListResult;
import com.kidozh.discuzhub.services.DiscuzApiService;
import com.kidozh.discuzhub.utilities.URLUtils;
import com.kidozh.discuzhub.utilities.UserPreferenceUtils;
import com.kidozh.discuzhub.utilities.bbsConstUtils;
import com.kidozh.discuzhub.utilities.networkUtils;
import com.kidozh.discuzhub.utilities.notificationUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AutoClearHistoriesWork extends Worker {
    private static final String TAG = AutoClearHistoriesWork.class.getSimpleName();
    private Context context;
    private forumUserBriefInfo userBriefInfo;
    bbsInformation bbsInformation;
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
