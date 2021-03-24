package com.example.ftptest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Objects;


public class Downloading extends AppCompatActivity {
    private FTPClient ftpCli = new FTPClient();
    private Livre book;
    private TextView Text;
    private ProgressBar progressBar;
    private String pathIn = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloading);

        Intent intent = getIntent();
        String bookString = intent.getStringExtra("bookString");
        String type = intent.getStringExtra("type"); // 0 : epub / 1 : pdf
        pathIn = "/data/data/com.example.ftptest/files/temp";

        book = new Livre(bookString);

        progressBar = findViewById(R.id.progressBar);
        Text = findViewById(R.id.textView);


        new telechargement().execute(type);



    }

    class telechargement extends AsyncTask<String, Void, String> {
        private static final String TAG = "Activity";


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... params) {
            String connect = "do in back ; ";
            int type = Integer.parseInt(params[0]);

            ftpCli.setAutodetectUTF8( true );

            try {

                ftpCli.connect("10.3.141.1");
                connect += " connecté ";

                try {
                    ftpCli.login("pi", "m0t2pa55e");

                    String filename = book.turnInDownloadable(book.getNomLivre()).replaceAll(" ", "_");

                    if (type == 0){
                        filename += ".epub";


                        boolean status = false;

                        // telechargement pas pareil pr < 10 et > 10
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            status = download_file_10_11(filename,"epub",ftpCli,(book.ftpURLepub).replaceAll(" ", "_"));
                        }
                        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            status = download_file(filename,"/storage/emulated/legacy/Download/CoLibri",ftpCli,(book.ftpURLepub).replaceAll(" ", "_") );
                        }
                        else {
                            connect += "\nerror with download";
                        }
                    }


                    else{
                        filename += ".pdf";


                        boolean status = false;


                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            status = download_file_10_11(filename,"pdf",ftpCli,(book.ftpURLepub).replaceAll(" ", "_"));
                        }
                        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            status = download_file(filename,"/storage/emulated/legacy/Download/CoLibri",ftpCli,book.ftpURLepub);

                        }
                        else {
                            connect += "\nerror with download";
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    connect += "pas login" + e;
                    return connect;
                }

            } catch (IOException e) {
                e.printStackTrace();
                connect += " pas connect " + e;
                return connect;
            }
            Log.d(TAG, "connect = " + connect);
            return connect;
        }

        @Override
        protected void onPostExecute(String result) {
            Integer in = R.layout.activity_main;

            super.onPostExecute(String.valueOf(in));

            //change affichage
            Text.setText("Téléchargement terminé\nVous trouverez votre livre dans les fichiers de votre téléphone");
            progressBar.setVisibility(View.INVISIBLE);


            try {
                ftpCli.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

    }


    // pour version < 10
    // meme principe que pour le stockage interne
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean download_file(String filename, String filePath, FTPClient ftpClient, String pathServer){
        String connect = "";
        connect += "\n" + filePath;

        try {
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.connect("10.3.141.1");
            ftpClient.login("pi", "m0t2pa55e");
            ftpClient.setAutodetectUTF8( true );
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            String path = String.valueOf(Environment.getExternalStorageDirectory()) + "/Download";
            File folder = new File(path , "CoLibri");
            if (folder.mkdir()) {
                connect += "File created: " + folder.getName();
            } else {
                connect += ("File already exists.");
            }
            File myObj = new File(folder.getAbsolutePath() , filename);
            if (myObj.createNewFile()) {
                connect += "File created: " + myObj.getName();
            } else {
                connect += ("File already exists.");
            }

            try (OutputStream os = new FileOutputStream(myObj.getAbsolutePath())) {

                // Download file from FTP server.
                boolean status = ftpClient.retrieveFile(pathServer+filename, os);
                connect += status + " \n" + ftpClient.getReplyString();


            } catch (IOException e) {
                e.printStackTrace();
                connect += " pas lecture file" +  e;
                return false;
            }
        }
        catch (IOException e){
            e.printStackTrace();
            connect += " pas ecrit file" +  e;
            return false;
        }
        return true;
    }



    // pour >= 10
    // different que pour le stockage interne
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean download_file_10_11(String filename, String type, FTPClient ftpClient, String pathServer){
        String connect = "";
        FileOutputStream fos;



        connect += "\n" + type;
        boolean test = ftpClient.isConnected();
        test = ftpClient.isAvailable();
        try {
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.connect("10.3.141.1");
            ftpClient.login("pi", "m0t2pa55e");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            

            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, type);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + "CoLibri");
            Uri uri1 = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            try {
                fos = (FileOutputStream) resolver.openOutputStream(Objects.requireNonNull(uri1));


                String t ;
                // Download file from FTP server.
                boolean status = ftpClient.retrieveFile(pathServer + filename, fos);
                connect += status + " \n" + ftpClient.getReplyString();

                Objects.requireNonNull(fos);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                connect += e;
            }


        } catch (IOException e) {
            e.printStackTrace();
            connect += " pas ecrit file" + e;
            return false;
        }
        return true;
    }

/////////////////////////////////////MARCHE PAS///////////////////////////////////////////////////
    // pour ajouter un téléchargement dans le csv
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void add_a_download(String filename, String filePath, FTPClient ftpClient, String pathServer){
        String connect = "";
        connect += "\n" + filePath;

        String toWrite = "";

        try {
            ftpClient.connect("10.3.141.1");
            boolean test = ftpClient.isConnected();
            test = ftpClient.isAvailable();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.login("pi", "m0t2pa55e");
            //  ftpClient.setControlEncoding("UTF-8");
            ftpClient.setAutodetectUTF8( true );
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            File myObj = new File(filePath + "/" + "Un_blesse.pdf");
            if (myObj.createNewFile()) {
                connect += "File created: " + myObj.getName();
            } else {
                connect += ("File already exists.");
            }
            File newObj = new File(filePath + "/bibliotheque2.csv");
            if (newObj.createNewFile()) {
                connect += "File created: " + newObj.getName();
            } else {
                connect += ("File already exists.");
            }

            String tcauib = myObj.getAbsolutePath();

            try (OutputStream os = new FileOutputStream(myObj.getAbsolutePath())) {

                // Download file from FTP server.
                boolean status = ftpClient.retrieveFile(pathServer + filename, os);
                connect += status + " \n" + ftpClient.getReplyString();

                BufferedReader in = new BufferedReader(new FileReader(myObj.getAbsolutePath()));
                BufferedWriter out = new BufferedWriter(new FileWriter(newObj.getAbsolutePath()));
                PrintWriter writer = new PrintWriter(newObj.getAbsolutePath(), "UTF-8");

                String ligne = "";
               /* try {
                    int n;

                    // read() function to read the
                    // byte of data

                    /*while ((ligne = in.readLine()) != null) {
                        // write() function to write
                        // the byte of data
                        toWrite+= ligne + "\n";
                    }
                    out.write(toWrite);*/

            /*        while((ligne = in.readLine()) != null){
                            ligne = in.readLine();

                                String data[] = ligne.split(";");
                                if (Integer.parseInt(data[0]) != book.getID()) {
                                    for (int j = 0; j < 10000; j++) {
                                    } // delay
                                    toWrite += ligne + "\n";
                                    // out.write(ligne + "\n");
                                } else {
                                    String ligne2 = String.valueOf(Integer.parseInt(data[0]));
                                    for (int i = 1; i < data.length; i++) {
                                        if (i == 5) {
                                            ligne2 += ";" + String.valueOf(Integer.parseInt(data[5]) + 1);
                                        } else {
                                            ligne2 += ";" + data[i];
                                        }
                                    }
                                    toWrite += ligne2 + "\n";
                                   // writer.println(ligne2);
                                    // out.write(ligne2 + "\n");
                                }
                            }*/

                    out.write(toWrite);
                    out.close();
                    writer.print(toWrite);
                    writer.close();
                    myObj.delete();
                    newObj.renameTo(myObj);
                    String oubefq = myObj.getAbsolutePath();
                    InputStream inp = new FileInputStream(myObj.getAbsolutePath()) ;
                  //  status = ftpClient.storeFile(filename, inp);
                    connect += status + " \n" + ftpClient.getReplyString();

               /* } catch (IOException e) {
                    e.printStackTrace();
                }*/


            } catch (IOException e) {
                e.printStackTrace();
                connect += " pas lecture file" +  e;

            }
        }
        catch (IOException e){
            e.printStackTrace();
            connect += " pas ecrit file" +  e;
        }
    }

/////////////////////////////////////////////////////////////////////////////////
}