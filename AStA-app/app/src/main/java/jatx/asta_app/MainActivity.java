package jatx.asta_app;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ar.com.daidalos.afiledialog.FileChooserActivity;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG_ACTIVITY = "MainActivity";
    private static final String PREFS_NAME = "astaPrefs";

    public static final String INTENT_FOLDER_CHOOSEN = "jatx.asta_app.FOLDER_CHOOSEN";

    private static final int REQUEST_CHOOSE_IMG = 501;
    private static final int REQUEST_OPEN_DIR_FOR_DOWNLOAD = 502;

    public static final int PERMISSION_SDCARD_REQUEST_CHOOSE_IMG = 1112;
    public static final int PERMISSION_SDCARD_REQUEST_OPEN_DIR_FOR_DOWNLOAD = 1113;
    public static final int PERMISSION_SDCARD_REQUEST_REFRESH_PROJECTS = 1114;
    public static final int PERMISSION_SDCARD_REQUEST_PREPARE_ASTA_FOLDER = 1115;

    public static final String PERMISSION_READ = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String[] PERMISSIONS = new String[]{PERMISSION_READ, PERMISSION_WRITE};

    private EditText editImgPath;
    private EditText editProjectsPath;
    private EditText editShellOutputDebian;
    private EditText editShellOutputGradle;

    private Spinner spinnerProjects;
    private Spinner spinnerModules;

    private Spinner spinnerGradleCmd;
    private EditText editCustomGradleCmd;

    private List<String> projectList;
    private List<List<String>> moduleList;

    private static final String CUSTOM_CMD = "Custom cmd";
    private static final String[] GRADLE_CMD_ARR =
            {"clean build", "assembleDebug", "assembleRelease", CUSTOM_CMD};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTabs();

        editImgPath = (EditText) findViewById(R.id.edit_img_path);
        editImgPath.setEnabled(false);
        editProjectsPath = (EditText) findViewById(R.id.edit_projects_path);
        editProjectsPath.setEnabled(false);

        editShellOutputDebian = (EditText) findViewById(R.id.edit_shell_output_debian);
        editShellOutputGradle = (EditText) findViewById(R.id.edit_shell_output_gradle);

        editImgPath.setText(getImgPath());
        editProjectsPath.setText(getProjectsFolder().getAbsolutePath());

        spinnerGradleCmd = (Spinner) findViewById(R.id.spinner_gradle_cmd);
        final ArrayAdapter<String> spinnerGradleCmdAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, GRADLE_CMD_ARR);
        spinnerGradleCmd.setAdapter(spinnerGradleCmdAdapter);
        editCustomGradleCmd = (EditText) findViewById(R.id.edit_custom_gradle_cmd);
        spinnerGradleCmd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (GRADLE_CMD_ARR[i].equals(CUSTOM_CMD)) {
                    editCustomGradleCmd.setVisibility(View.VISIBLE);
                } else {
                    editCustomGradleCmd.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        findViewById(R.id.button_choose_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImg(null, false);
            }
        });

        findViewById(R.id.button_download_img).setOnClickListener(new View.OnClickListener() {
            private AlertDialog dialog;
            private EditText editImgDownloadPath;
            private Button buttonDownload;
            private TextView textFreeSize;
            private String imgSavePath;

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater layoutInflater = getLayoutInflater();
                View v = layoutInflater.inflate(R.layout.dialog_download_img, null);

                editImgDownloadPath = (EditText) v.findViewById(R.id.edit_img_download_path);
                editImgDownloadPath.setEnabled(false);

                textFreeSize = (TextView) v.findViewById(R.id.text_free_size);

                buttonDownload = (Button) v.findViewById(R.id.button_download);
                buttonDownload.setEnabled(false);

                buttonDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File imgFile = new File(imgSavePath);
                        if (imgFile.exists()) {
                            Toast.makeText(MainActivity.this, "img file already exists", Toast.LENGTH_LONG).show();
                            return;
                        }
                        dialog.dismiss();
                        downloadImg(imgSavePath);
                    }
                });

                v.findViewById(R.id.button_choose_folder).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openDirForDownload(null, false);
                    }
                });

                builder.setView(v);

                final BroadcastReceiver folderChoosenReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String dirPath = intent.getStringExtra("path");
                        //Toast.makeText(MainActivity.this, "path: " + path, Toast.LENGTH_LONG).show();
                        imgSavePath = dirPath + File.separator + "debianOnAndroid.img";
                        editImgDownloadPath.setText(imgSavePath);
                        StatFs stat = new StatFs(dirPath);
                        long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
                        long megAvailable = bytesAvailable / (1024 * 1024);
                        long megNeed = 4096;
                        textFreeSize.setText("Need: " + megNeed + "MB\nFree: " + megAvailable + " MB");
                        buttonDownload.setEnabled(megAvailable >= megNeed);
                    }
                };
                IntentFilter folderChoosenFilter = new IntentFilter(INTENT_FOLDER_CHOOSEN);
                registerReceiver(folderChoosenReceiver, folderChoosenFilter);

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        unregisterReceiver(folderChoosenReceiver);
                        dialogInterface.dismiss();
                    }
                });

                dialog = builder.create();
                dialog.show();
            }
        });

        findViewById(R.id.button_install_busybox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=stericson.busybox")));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=stericson.busybox")));
                }
            }
        });

        findViewById(R.id.button_mount_debian).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mountDebian();
            }
        });

        findViewById(R.id.button_umount_debian).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                umountDebian();
            }
        });

        findViewById(R.id.button_refresh_projects).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshProjects(false);
            }
        });

        findViewById(R.id.button_gradle_exec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int projectPosition = spinnerProjects.getSelectedItemPosition();
                int modulePosition =  spinnerModules.getSelectedItemPosition();

                String project = projectList.get(projectPosition);
                String module = moduleList.get(projectPosition).get(modulePosition);
                String gradleCmd = GRADLE_CMD_ARR[spinnerGradleCmd.getSelectedItemPosition()];
                if (gradleCmd.equals(CUSTOM_CMD)) {
                    gradleCmd = editCustomGradleCmd.getText().toString().trim();
                }
                if (gradleCmd.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Gradle cmd is empty", Toast.LENGTH_LONG).show();
                    return;
                }

                execChrootGradle(project, module, "'" + gradleCmd + "'");
            }
        });
    }

    private void refreshProjects(boolean permissionsOk) {
        if (!permissionsOk) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (checkSelfPermission(PERMISSION_READ)
                        == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(PERMISSIONS, PERMISSION_SDCARD_REQUEST_REFRESH_PROJECTS);
                } else {
                    refreshProjects(true);
                }
            } else {
                refreshProjects(true);
            }
        } else {
            prepareAStAFolder(true);

            File[] projectDirs = getProjectsFolder().listFiles();

            projectList = new ArrayList<>();
            moduleList = new ArrayList<>();

            for (File projectDir : projectDirs) {
                File buildFile = new File(projectDir, "build.gradle");
                File settingsFile = new File(projectDir, "settings.gradle");
                File imlFile = new File(projectDir, projectDir.getName() + ".iml");

                if (projectDir.isDirectory() &&
                        buildFile.exists() && buildFile.isFile() &&
                        settingsFile.exists() && settingsFile.isFile() &&
                        imlFile.exists() && settingsFile.exists()) {

                    try {
                        Scanner sc = new Scanner(settingsFile);
                        String settingsContent = sc.useDelimiter("\\z").next().trim();
                        if (!settingsContent.startsWith("include")) continue;
                        String[] moduleStrings = settingsContent.split("\\s");

                        List<String> projectModuleList = new ArrayList<>();

                        for (String moduleName : moduleStrings) {
                            if (moduleName.startsWith("':")
                                    && (moduleName.endsWith("'") ||
                                    moduleName.endsWith("',"))) {

                                moduleName = moduleName
                                        .replace("':", "")
                                        .replace("',", "'")
                                        .replace("'", "");

                                File moduleDir = new File(projectDir, moduleName);
                                if (moduleDir.exists() && moduleDir.isDirectory()) {
                                    File buildModuleFile = new File(moduleDir, "build.gradle");
                                    if (buildModuleFile.exists() && buildModuleFile.isFile()) {
                                        projectModuleList.add(moduleName);
                                    }
                                }
                            }
                        }

                        if (projectModuleList.size() > 0) {
                            projectList.add(projectDir.getName());
                            moduleList.add(projectModuleList);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }

            int i = 0;
            for (String project : projectList) {
                List<String> projectModuleList = moduleList.get(i);
                i++;
                for (String module : projectModuleList) {
                    Log.e("module", project + ":" + module);
                }
            }

            if (projectList.size() == 0) {
                findViewById(R.id.text_no_projects).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_project_spinners).setVisibility(View.GONE);
                findViewById(R.id.button_gradle_exec).setEnabled(false);
            } else {
                findViewById(R.id.text_no_projects).setVisibility(View.GONE);
                findViewById(R.id.layout_project_spinners).setVisibility(View.VISIBLE);
                findViewById(R.id.button_gradle_exec).setEnabled(true);

                initProjectSpinners();
            }
        }
    }

    private void initProjectSpinners() {
        spinnerProjects = (Spinner) findViewById(R.id.spinner_projects);

        String[] projectArr = projectList.toArray(new String[projectList.size()]);

        ArrayAdapter<String> spinnerProjectsAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, projectArr);
        spinnerProjects.setAdapter(spinnerProjectsAdapter);

        spinnerModules = (Spinner) findViewById(R.id.spinner_modules);
        fillSpinnerModules(0);

        spinnerProjects.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fillSpinnerModules(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void fillSpinnerModules(int projectPosition) {
        String[] modulesArr = moduleList.get(projectPosition).toArray(new String[moduleList.get(projectPosition).size()]);

        ArrayAdapter<String> spinnerModulesAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modulesArr);
        spinnerModules.setAdapter(spinnerModulesAdapter);
    }

    private void initTabs() {
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setIndicator("Debian");
        tabSpec.setContent(R.id.tab1);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator("Gradle");
        tabSpec.setContent(R.id.tab2);
        tabHost.addTab(tabSpec);

     }

    private void downloadImg(final String imgSavePath) {
        final ProgressDialog progressDialog;

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(4095);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);
        progressDialog.show();

        final BroadcastReceiver progressUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int mbProgress = intent.getIntExtra("mbProgress", -1);
                progressDialog.setProgress(mbProgress);
            }
        };
        IntentFilter progressUpdateFilter = new IntentFilter(ImgDownloadIntentService.INTENT_UPDATE_PROGRESS);
        registerReceiver(progressUpdateReceiver, progressUpdateFilter);

        BroadcastReceiver downloadFinishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String result = intent.getStringExtra("result");
                Toast.makeText(MainActivity.this, "Download finished: " + result, Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
                unregisterReceiver(progressUpdateReceiver);
                unregisterReceiver(this);
                if (result.equals("success")) {
                    saveImgPath(imgSavePath);
                    editImgPath.setText(imgSavePath);
                }
            }
        };
        IntentFilter downloadFinishedFilter = new IntentFilter(ImgDownloadIntentService.INTENT_DOWNLOAD_FINISHED);
        registerReceiver(downloadFinishedReceiver, downloadFinishedFilter);

        Intent intent = new Intent(this, ImgDownloadIntentService.class);
        intent.putExtra("imgSavePath", imgSavePath);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            switch (requestCode) {
                case PERMISSION_SDCARD_REQUEST_CHOOSE_IMG:
                    chooseImg(null, true);
                    return;
                case PERMISSION_SDCARD_REQUEST_OPEN_DIR_FOR_DOWNLOAD:
                    openDirForDownload(null, true);
                    return;
                case PERMISSION_SDCARD_REQUEST_REFRESH_PROJECTS:
                    refreshProjects(true);
                    return;
                case PERMISSION_SDCARD_REQUEST_PREPARE_ASTA_FOLDER:
                    prepareAStAFolder(true);
                    return;
            }
        } else {
            Toast.makeText(this, "No SDCard access", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(LOG_TAG_ACTIVITY, "on activity result");
        if (resultCode!=RESULT_OK) return;

        String filePath = "";

        Bundle bundle = data.getExtras();
        if(bundle != null) {
            File file = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
            filePath = file.getAbsolutePath();
        }

        File f = new File(filePath);

        if (requestCode==REQUEST_CHOOSE_IMG&&resultCode==RESULT_OK) {
            chooseImg(f, true);
        } else if (requestCode==REQUEST_OPEN_DIR_FOR_DOWNLOAD&&resultCode==RESULT_OK) {
            openDirForDownload(f, true);
        }
    }

    private void chooseImg(File selectedFile, boolean permissionsOk) {
        if (!permissionsOk) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (checkSelfPermission(PERMISSION_READ)
                        == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(PERMISSIONS, PERMISSION_SDCARD_REQUEST_CHOOSE_IMG);
                } else {
                    chooseImg(null, true);
                }
            } else {
                chooseImg(null, true);
            }
        } else if (selectedFile==null) {
            Intent intent = new Intent(this, FileChooserActivity.class);
            intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, Environment.getExternalStorageDirectory());
            intent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, false);
            intent.putExtra(FileChooserActivity.INPUT_REGEX_FILTER, ".*\\.img");
            startActivityForResult(intent, REQUEST_CHOOSE_IMG);
        } else {
            Log.i(LOG_TAG_ACTIVITY, "file: " + selectedFile.getAbsolutePath());
            editImgPath.setText(selectedFile.getAbsolutePath());
            saveImgPath(selectedFile.getAbsolutePath());
        }
    }

    private void openDirForDownload(File selectedDir, boolean permissionsOk) {
        if (!permissionsOk) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (checkSelfPermission(PERMISSION_READ)
                        == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(PERMISSIONS, PERMISSION_SDCARD_REQUEST_OPEN_DIR_FOR_DOWNLOAD);
                } else {
                    openDirForDownload(null, true);
                }
            } else {
                openDirForDownload(null, true);
            }
        } else if (selectedDir==null) {
            Intent intent = new Intent(this, FileChooserActivity.class);
            intent.putExtra(FileChooserActivity.INPUT_START_FOLDER, Environment.getExternalStorageDirectory());
            intent.putExtra(FileChooserActivity.INPUT_FOLDER_MODE, true);
            startActivityForResult(intent, REQUEST_OPEN_DIR_FOR_DOWNLOAD);
        } else {
            Log.i(LOG_TAG_ACTIVITY, "dir: " + selectedDir.getAbsolutePath());
            Intent intent = new Intent();
            intent.setAction(INTENT_FOLDER_CHOOSEN);
            intent.putExtra("path", selectedDir.getAbsolutePath());
            sendBroadcast(intent);
        }
    }

    private String getImgPath() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, 0);
        return sp.getString("imgPath", "");
    }

    private void saveImgPath(String path) {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("imgPath", path);
        editor.commit();
    }

    private void prepareAStAFolder(boolean permissionsOk) {
        if (!permissionsOk) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (checkSelfPermission(PERMISSION_READ)
                        == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(PERMISSIONS, PERMISSION_SDCARD_REQUEST_PREPARE_ASTA_FOLDER);
                } else {
                    prepareAStAFolder(true);
                }
            } else {
                prepareAStAFolder(true);
            }
        } else {
            File astaDir = getAStAFolder();
            astaDir.mkdirs();
            File projectsDir = getProjectsFolder();
            projectsDir.mkdirs();
            File scriptsDir = getScriptsFolder();
            scriptsDir.mkdirs();

            try {
                final String scriptName = "mountDebian.sh";
                File scriptFile = new File(scriptsDir, scriptName);
                PrintWriter pw = new PrintWriter(scriptFile);
                String scriptContent = getAssetAsString(scriptName);
                scriptContent = scriptContent
                        .replace("{astaFolder}", getAStAFolder().getAbsolutePath())
                        .replace("{imgFile}", getImgPath());
                pw.println(scriptContent);
                pw.flush();
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                final String scriptName = "umountDebian.sh";
                File scriptFile = new File(scriptsDir, scriptName);
                PrintWriter pw = new PrintWriter(scriptFile);
                String scriptContent = getAssetAsString(scriptName);
                pw.println(scriptContent);
                pw.flush();
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                final String scriptName = "gradleExec.sh";
                File scriptFile = new File(scriptsDir, scriptName);
                PrintWriter pw = new PrintWriter(scriptFile);
                String scriptContent = getAssetAsString(scriptName);
                pw.println(scriptContent);
                pw.flush();
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                final String scriptName = "chrootGradleExec.sh";
                File scriptFile = new File(scriptsDir, scriptName);
                PrintWriter pw = new PrintWriter(scriptFile);
                String scriptContent = getAssetAsString(scriptName);
                scriptContent = scriptContent
                        .replace("{astaFolder}", getAStAFolder().getAbsolutePath());
                pw.println(scriptContent);
                pw.flush();
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getAStAFolder() {
        return new File(Environment.getExternalStorageDirectory(), "AStA");
    }

    private File getProjectsFolder() {
        return new File(getAStAFolder(), "Projects");
    }

    private File getScriptsFolder() {
        return new File(getAStAFolder(), "scripts");
    }

    private String getAssetAsString(String fileName) {
        try {
            AssetManager assetManager = getAssets();
            InputStream is = assetManager.open(fileName);
            Scanner sc = new Scanner(is);
            String str = sc.useDelimiter("\\z").next();
            sc.close();
            return str;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mountDebian() {
        if (getImgPath().isEmpty()) {
            Toast.makeText(this, "Img path not set", Toast.LENGTH_LONG).show();
            return;
        }

        prepareAStAFolder(false);

        execAStAScript("mountDebian.sh");
    }

    private void umountDebian() {
        if (getImgPath().isEmpty()) {
            Toast.makeText(this, "Img path not set", Toast.LENGTH_LONG).show();
            return;
        }

        prepareAStAFolder(false);

        execAStAScript("umountDebian.sh");
    }

    private void execAStAScript(final String scriptName) {
        findViewById(R.id.button_mount_debian).setEnabled(false);
        findViewById(R.id.button_umount_debian).setEnabled(false);
        new Thread() {
            @Override
            public void run() {
                try {

                    File scriptFile = new File(getScriptsFolder(), scriptName);
                    final String cmd = "sh " + scriptFile.getAbsolutePath();

                    Process suProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});

                    BufferedReader stdInput = new BufferedReader(new
                            InputStreamReader(suProcess.getInputStream()));

                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(suProcess.getErrorStream()));

                    final StringBuilder sb = new StringBuilder();

                    String s1 = null;
                    String s2 = null;
                    while ((s1 = stdInput.readLine()) != null || (s2 = stdError.readLine()) != null) {
                        if (s1 != null) {
                            sb.append(s1 + "\n");
                        }
                        if (s2 != null) {
                            sb.append(s2 + "\n");
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editShellOutputDebian.setText(sb.toString());
                                final ScrollView scrollOutputDebian = (ScrollView) findViewById(R.id.scroll_output_debian);
                                scrollOutputDebian.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollOutputDebian.smoothScrollTo(0, editShellOutputDebian.getBottom());
                                    }
                                });
                            }
                        });
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "exec script error", Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.button_mount_debian).setEnabled(true);
                            findViewById(R.id.button_umount_debian).setEnabled(true);
                        }
                    });
                }
            }
        }.start();
    }

    private void execChrootGradle(final String project, final String module, final String gradleCmd) {
        findViewById(R.id.button_gradle_exec).setEnabled(false);
        new Thread() {
            @Override
            public void run() {
                try {
                    File scriptFile = new File(getScriptsFolder(), "chrootGradleExec.sh");
                    final String cmd = "sh " + scriptFile.getAbsolutePath() +
                            " " + project + " " + module + " " + gradleCmd;

                    Process suProcess = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});

                    BufferedReader stdInput = new BufferedReader(new
                            InputStreamReader(suProcess.getInputStream()));

                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(suProcess.getErrorStream()));

                    final StringBuilder sb = new StringBuilder();

                    String s1 = null;
                    String s2 = null;
                    while ((s1 = stdInput.readLine()) != null || (s2 = stdError.readLine()) != null) {
                        if (s1 != null) {
                            sb.append(s1 + "\n");
                        }
                        if (s2 != null) {
                            sb.append(s2 + "\n");
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editShellOutputGradle.setText(sb.toString());
                                final ScrollView scrollOutputGradle = (ScrollView) findViewById(R.id.scroll_output_gradle);
                                scrollOutputGradle.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollOutputGradle.smoothScrollTo(0, editShellOutputGradle.getBottom());
                                    }
                                });
                            }
                        });
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "exec gradle script error", Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.button_gradle_exec).setEnabled(true);
                        }
                    });
                }
            }
        }.start();
    }
}
