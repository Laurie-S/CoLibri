package com.example.ftptest;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;

public class On_livre extends AppCompatActivity {
    private FTPClient ftpCli = new FTPClient();
    private Livre book;
    private String lien;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_livre);

        Intent intent = getIntent();
        String bookString = intent.getStringExtra("bookString");
        lien = intent.getStringExtra("lien");

        // on  verifie si la permission est acordé
        int permission = 0;
        permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        book = new Livre(bookString);

        TextView Nom = findViewById(R.id.Titre_livre);
        TextView Auteur = findViewById(R.id.Auteur);
        TextView Resume = findViewById(R.id.Resume);
        ImageView Image = findViewById(R.id.image_livre);
        final Button bouton1 = findViewById(R.id.button1);
        final Button bouton2 = findViewById(R.id.button2);
        final Button bouton3 = findViewById(R.id.button3);




        Nom.setText(book.getNomLivre());
        Auteur.setText(book.getNomAuteur());
        Resume.setText(book.getResume());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File my = new File(lien);
            Image.setImageBitmap(BitmapFactory.decodeFile(lien));
        }

        if(book.getAudioOuPas() == 0){ // livre simple
            bouton3.setVisibility(Button.INVISIBLE);
            bouton1.setText("Télécharger en epub (" + book.getTailleEpub() + "Ko)" );
            bouton2.setText("Télécharger en pdf (" + book.getTaillePdf() + "Ko)");
            final int finalPermission = permission;
            bouton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(requestPermission()) {
                        openDL("0"); // lance le telechargement epub
                    }
                    else{
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(On_livre.this, permissions, finalPermission);
                        }
                    }

                }
            });
            bouton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(requestPermission()) {
                        openDL("1"); // lance le telechargement pdf
                    }
                    else{
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(On_livre.this, permissions, finalPermission);
                        }
                    }
                }
            });

        }
        else if(book.getAudioOuPas() == 1){ // livre audio
            bouton1.setVisibility(Button.INVISIBLE);
            bouton3.setVisibility(Button.INVISIBLE);
            bouton2.setText("Télécharger en audio (copier le lien)");
            bouton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // copie le lien dans le presse papier
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("lien livre audio", book.getUrlDL());
                    clipboard.setPrimaryClip(clip);
                    bouton2.setText("lien copié\ncollez le dans le navigateur");
                }
            });
        }
        else{ // les deux
            bouton1.setText("Télécharger en epub (" + book.getTailleEpub() + "Ko)" );
            bouton2.setText("Télécharger en pdf (" + book.getTaillePdf() + "Ko)");
            bouton3.setText("Télécharger en audio (copier le lien)");
            final int finalPermission = permission;
            bouton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(requestPermission()) {
                        openDL("0");
                    }
                    else{
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(On_livre.this, permissions, finalPermission);
                        }
                    }

                }
            });
            bouton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(requestPermission()) {
                        openDL("1");
                    }
                    else{
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            ActivityCompat.requestPermissions(On_livre.this, permissions, finalPermission);
                        }
                    }
                }
            });
            bouton3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("lien livre audio", book.getUrlDL());
                    clipboard.setPrimaryClip(clip);
                    bouton3.setText("lien copié\ncollez le dans le navigateur");
                }
            });
        }



    }

    private boolean requestPermission(){
        boolean request=true;
        String[] permissions={Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};
        if (permissions.length!=0){
            ActivityCompat.requestPermissions(this,permissions,102);
            request= true;
        }

        else{
            request=false;
        }

        return request;

    }


    public void openDL(String type){
        Intent intent = new Intent(this,Downloading.class);



        String str = book.toString();
        String pathIn = "/data";
        String data[] = lien.split("/");
        for (int i = 1 ; i < data.length-2 ; i++){
            pathIn += "/"+ data[i];
        }


        intent.putExtra("bookString",str);
        intent.putExtra("type", type);
        intent.putExtra("pathIn", pathIn);

        startActivity(intent);
    }

}