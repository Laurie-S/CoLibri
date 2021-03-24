package com.example.ftptest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context ctx;
    public FTPClient ftpClient = new FTPClient();
    private int permiss = 0;
    private String pathIn;
    private boolean clicked;


    Vector<Livre> Bibliotheque = new Vector();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this;

        // Demande de permission pour l'acces au stockage
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, permiss);
        } else {
            permiss = 1;
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new Connexion().execute("");

    }


    @Override   // Menu en haut (recherche et tri)
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_1, menu);

        // pour la recherche
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView search = (SearchView) searchItem.getActionView();
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }


    @Override // pour le tri
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId(); // on recupere le type de tri selectionné
        switch (id){
            case R.id.AuteurCroissant:
                mAdapter.getFilter().filter("AutCrois"); // on a un filtre pour chaque type de tri
                return true;
            case R.id.AuteurDecroissant:
                mAdapter.getFilter().filter("AutDec");
                return true;
            case R.id.TitreCroissant:
                mAdapter.getFilter().filter("TitCrois");
                return true;
            case R.id.TitreDecroissant:
                mAdapter.getFilter().filter("TitDec");
                return true;
            case R.id.DateCroissant:
                mAdapter.getFilter().filter("DateCrois");
                return true;
            case R.id.DateDecroissant:
                mAdapter.getFilter().filter("DateDec");
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    class Connexion extends AsyncTask<String, Void, String> {
        private static final String TAG = "Activity";

        @Override
        protected String doInBackground(String... params) {
            String connect = "do in back ; "; // debug

            // pour lire les fichier en UTF-8 (avec accents)
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setAutodetectUTF8(true);



            testCo(); // test de la connection au serveur

            ftpClient.setConnectTimeout(2000);
            try {
                ftpClient.login("pi", "m0t2pa55e");

                String filename = "Bibliotheque.csv";
                pathIn = getFilesDir().getPath().toString(); // on récupere le chemin de l'appli dans le stockage interne
                                                             // (invisible pour l'utilisateur)
                String filePath = pathIn + "/" + filename;


                boolean status = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // ne marche que pour > KITKAT
                    status = download_file(filename, filePath, ftpClient, "");
                }

                if (status) { // on lit le fichier et on entre chaque livre dans le vecteur bibliotheque
                    setBibliotheque(filePath);
                } else {
                    connect += "\nerror with download";
                }


            } catch (IOException e) {
                e.printStackTrace();
                connect += "pas login" + e;
                return connect;
            }

            Log.d(TAG, "connect = " + connect);
            return connect;
        }

        @Override
        protected void onPostExecute(String result) {
            Integer in = R.layout.activity_main;

            super.onPostExecute(String.valueOf(in));
            // lance le recycler view
            buildRecyclerView();

/*
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }*/


        }


        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }

    public void setBibliotheque(String path) throws FileNotFoundException {
        BufferedReader in = new BufferedReader(new FileReader(path));
        String ligne = "";
        try {
            while ((ligne = in.readLine()) != null) {
                if (!ligne.equals("")) {
                    Livre Book1 = new Livre(ligne);
                    Bibliotheque.add(Book1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        File sup_file = new File(path);
        sup_file.delete();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean download_file(String filename, String filePath, FTPClient ftpClient, String pathServer) {
        String connect = ""; // debug
        connect += "\n" + filePath;

        try {       // on cree le fichier si il le faut
            File myObj = new File(filePath);
            if (myObj.createNewFile()) {
                connect += "File created: " + myObj.getName();
            } else {
                connect += ("File already exists.");
            }

            try (OutputStream os = new FileOutputStream(filePath)) { // on cree le stream du fichier

                // On recupere le fichier
                boolean status = ftpClient.retrieveFile(pathServer + filename, os);
                connect += status + " \n" + ftpClient.getReplyString();

            } catch (IOException e) {
                e.printStackTrace();
                connect += " pas lecture file" + e;
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            connect += " pas ecrit file" + e;
            return false;
        }
        return true;
    }

    public void changeClick() {
        clicked = true;
    }

    public boolean isClicked() {
        if (clicked) {
            clicked = false;
            return true;
        }
        return clicked;
    }



        public void testCo () {
            final Button bouton = findViewById(R.id.retry);
            final TextView AttenteCo = findViewById(R.id.AttenteCo);
            final TextView erreurConnexion = findViewById(R.id.erreurConnexion);
            final ProgressBar bar = findViewById(R.id.progressBar2);
            int i = 1;
            clicked = false;
            Display display = getWindowManager().getDefaultDisplay();
            int orientation = getResources().getConfiguration().orientation;
            ftpClient.setConnectTimeout(2000); // limite le temps de timeout a 2s
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR); // no screen rotation
            while (i == 1) {    // tant qu'on a pas reussi

                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() { // pour changer ce qui est afficher dans un doInBackground
                            bar.setVisibility(ProgressBar.VISIBLE);
                            AttenteCo.setVisibility(TextView.VISIBLE);
                            bouton.setVisibility(Button.INVISIBLE);
                            erreurConnexion.setVisibility(TextView.INVISIBLE);
                        }
                    });
                    ftpClient.connect("10.3.141.1"); // on essaye de se co
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bar.setVisibility(ProgressBar.GONE);
                            bouton.setVisibility(Button.GONE);
                            AttenteCo.setVisibility(TextView.GONE);
                            erreurConnexion.setVisibility(TextView.GONE);
                        }
                    });
                    i = 0;
                } catch (IOException e) { // si il y a eu une erreur (pas de connexion)
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bar.setVisibility(ProgressBar.INVISIBLE);
                            AttenteCo.setVisibility(TextView.INVISIBLE);
                            erreurConnexion.setVisibility(TextView.VISIBLE);
                            bouton.setVisibility(Button.VISIBLE);
                        }
                    });
                    bouton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { // quand on appuie sur le bouton
                            changeClick(); // on change la valeur de la variable clicked en true
                        }
                    });
                    while (!isClicked()) { // tant que clicked n'est pas egal à true
                    }                   // on attend que l'utilisateur appuie sur réessayer
                }
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR); //screen rotation
        }

    public void buildRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); // il change pas de taille
        mAdapter = new Adapter(Bibliotheque,ftpClient); // on donne les infos a l'adapter
        mLayoutManager = new LinearLayoutManager(this);

        ///////////////////////////////pour le nombre de colone////////////////////////////////////
        int numbrerRow;
        Display display = getWindowManager().getDefaultDisplay();
        int orientation = getResources().getConfiguration().orientation;
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // calcul de la taille de l'ecran en pouces
        float yInches= metrics.heightPixels/metrics.ydpi;
        float xInches= metrics.widthPixels/metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);

        if (diagonalInches>=6.5){ // superieur a 6.5 pouces (tablette)
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                numbrerRow = 5;
            } else {
                numbrerRow = 3;
            }
        }else{ // telephone
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                numbrerRow = 3;
            } else {
                numbrerRow = 2;
            }
        }

        //////////////////////////////////////////


        // on display en grille
        GridLayoutManager grid = new GridLayoutManager(ctx,numbrerRow,GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(grid);


        recyclerView.setAdapter(mAdapter);

        // quand on clique sur un livre
        mAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openLivre(position);

            }
        });
    }

    public void openLivre(int position){
        Intent intent = new Intent(this,On_livre.class); // pour passer des info d'un layout a un autre

        //Livre book1 = Bibliotheque.elementAt(position);
        Livre book1 = mAdapter.getLivre(position);
        String str = book1.toString();
        String image = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            image = book1.launchDownload(this, ftpClient); // on telecharge l'image avant de partir du layout
        }
        intent.putExtra("bookString",str);
        intent.putExtra("lien", image);

        startActivity(intent);
    }





}


