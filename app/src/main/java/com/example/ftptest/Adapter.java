package com.example.ftptest;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.net.ftp.FTPClient;

import java.util.Vector;

public class Adapter extends RecyclerView.Adapter<Adapter.ExempleViewHolder> implements Filterable {
    private Vector<Livre> Liste;
    private OnItemClickListener mListener;
    private Vector<Livre> ListeFull;
    public Context ctx;
    public FTPClient ftpClient;
    public Livre book1;



    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public Livre getLivre(int position) {
        return Liste.elementAt(position);
    }

    public static class ExempleViewHolder extends RecyclerView.ViewHolder {
        public TextView Text;
        public TextView Text2;
        public ImageView image;

        public ExempleViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            Text = itemView.findViewById(R.id.Titre1);
            Text2 = itemView.findViewById(R.id.Auteur1);
            image = itemView.findViewById(R.id.image1);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // si on clique sur un livre
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public Adapter(Vector<Livre> Liste1, FTPClient ftp) {
        ListeFull = new Vector<Livre>(Liste1);
        Liste = Liste1;
        ftpClient = ftp;
    }

    @NonNull
    @Override
    public ExempleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_item, parent, false);
        ctx = v.getContext();

        ExempleViewHolder evh = new ExempleViewHolder(v, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ExempleViewHolder holder, int position) {
        Livre currentItem = Liste.elementAt(position);

        holder.Text.setText(currentItem.getNomLivre());
        holder.Text2.setText(currentItem.getNomAuteur());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if(currentItem.isDownloaded()){ // si l'image est déja telechargé
                holder.image.setImageBitmap(currentItem.getBitm());
            }
            else { // sinon on la télécharge
                holder.image.setImageBitmap(currentItem.getBitmap(ctx,ftpClient));
            }
            if(position > 20){ // on vide les bitmap enregistrés dans la mémoire pour ne pas qu'on la remplisse
                Livre item_20 = Liste.elementAt(position-20);
                item_20.bitmapVoid();
            }
            else if(position < Liste.size()-21){
                Livre item_20 = Liste.elementAt(position+20);
                item_20.bitmapVoid();
            }
        }


    }

    @Override
    public int getItemCount() {
        return Liste.size();
    }

    @Override
    public Filter getFilter() {
        return exempleFilter;

    }
// pour la recherche / tri
    private Filter exempleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Vector<Livre> fiteredList = new Vector<>();

            // Si il n y a rien on met tout
            if (constraint == null || constraint.length() == 0) {
                fiteredList.addAll(ListeFull);
            }
      /////////////////////////////////  TRI  //////////////////////////////////////////

            else if(constraint == "AutCrois"){ // en fonction du filtre renvoyé
                Livre[] tmpLivre = new Livre[ListeFull.size()];
                for(int j = 0; j < ListeFull.size() ; j++){
                    tmpLivre[j] = ListeFull.elementAt(j);
                }
                for(int i = 0; i < ListeFull.size() ; i++){
                    for(int j = 1; j < ListeFull.size() ; j++){
                        if(tmpLivre[j].compareAuteur(tmpLivre[j-1]) == 1){
                            Livre tmp = tmpLivre[j];
                            tmpLivre[j] = tmpLivre[j-1];
                            tmpLivre[j-1] = tmp;
                        }

                    }
                }
                for(int j = 0; j < ListeFull.size() ; j++){
                    fiteredList.add(tmpLivre[j]);
                }
            }
            else if(constraint == "AutDec"){
                Livre[] tmpLivre = new Livre[ListeFull.size()];
                for(int j = 0; j < ListeFull.size() ; j++){
                    tmpLivre[j] = ListeFull.elementAt(j);
                }
                for(int i = 0; i < ListeFull.size() ; i++){
                    for(int j = 1; j < ListeFull.size() ; j++){
                        if(tmpLivre[j].compareAuteur(tmpLivre[j-1]) == -1){
                            Livre tmp = tmpLivre[j];
                            tmpLivre[j] = tmpLivre[j-1];
                            tmpLivre[j-1] = tmp;
                        }
                    }
                }
                for(int j = 0; j < ListeFull.size() ; j++){
                    fiteredList.add(tmpLivre[j]);
                }
            }
            else if(constraint == "TitCrois"){
                Livre[] tmpLivre = new Livre[ListeFull.size()];
                for(int j = 0; j < ListeFull.size() ; j++){
                    tmpLivre[j] = ListeFull.elementAt(j);
                }
                for(int i = 0; i < ListeFull.size() ; i++){
                    for(int j = 1; j < ListeFull.size() ; j++){
                        if(tmpLivre[j].compareTitre(tmpLivre[j-1]) == 1){
                            Livre tmp = tmpLivre[j];
                            tmpLivre[j] = tmpLivre[j-1];
                            tmpLivre[j-1] = tmp;
                        }

                    }
                }
                for(int j = 0; j < ListeFull.size() ; j++){
                    fiteredList.add(tmpLivre[j]);
                }
            }
            else if(constraint == "TitDec"){
                Livre[] tmpLivre = new Livre[ListeFull.size()];
                for(int j = 0; j < ListeFull.size() ; j++){
                    tmpLivre[j] = ListeFull.elementAt(j);
                }
                for(int i = 0; i < ListeFull.size() ; i++){
                    for(int j = 1; j < ListeFull.size() ; j++){
                        if(tmpLivre[j].compareTitre(tmpLivre[j-1]) == -1){
                            Livre tmp = tmpLivre[j];
                            tmpLivre[j] = tmpLivre[j-1];
                            tmpLivre[j-1] = tmp;
                        }
                    }
                }
            for(int j = 0; j < ListeFull.size() ; j++){
                fiteredList.add(tmpLivre[j]);
            }
        }

            else if(constraint == "DateCrois"){

                Livre[] tmpLivre = new Livre[ListeFull.size()];
                for(int j = 0; j < ListeFull.size() ; j++){
                    tmpLivre[j] = ListeFull.elementAt(j);
                }

                for(int i = 0; i < ListeFull.size() ; i++){
                    for(int j = 1; j < ListeFull.size() ; j++){
                        if(tmpLivre[j].compareDate(tmpLivre[j-1]) == 1){
                            Livre tmp = tmpLivre[j];
                            tmpLivre[j] = tmpLivre[j-1];
                            tmpLivre[j-1] = tmp;
                        }

                    }
                }
                for(int j = 0; j < ListeFull.size() ; j++){
                    fiteredList.add(tmpLivre[j]);
                }
            }
            else if(constraint == "DateDec"){
                Livre[] tmpLivre = new Livre[ListeFull.size()];
                for(int j = 0; j < ListeFull.size() ; j++){
                    tmpLivre[j] = ListeFull.elementAt(j);
                }
                for(int i = 0; i < ListeFull.size() ; i++){
                    for(int j = 1; j < ListeFull.size() ; j++){
                        if(tmpLivre[j].compareDate(tmpLivre[j-1]) == -1){
                            Livre tmp = tmpLivre[j];
                            tmpLivre[j] = tmpLivre[j-1];
                            tmpLivre[j-1] = tmp;
                        }
                    }
                }
                for(int j = 0; j < ListeFull.size() ; j++){
                    fiteredList.add(tmpLivre[j]);
                }
            }
//////////////////////////////////////// RECHERCHE /////////////////////////////////////

            else {
                String filterPatern = constraint.toString().toLowerCase().trim();


                for (Livre book : ListeFull) {
                    String genre = "";
                    for (int i = 0; i < (book.genres).length; i++) {
                        genre += book.genres[i] + " ";
                    }
                    if (book.getAudioOuPas() != 0) { // on peut aussi chercher que les livres audio
                        genre += "audio";
                    }

                    // on cherche dans le resumé, le nom, l auteur, la date et les genres
                    if (book.getNomLivre().toLowerCase().contains(filterPatern)
                            || book.getNomAuteur().toLowerCase().contains(filterPatern)
                            || book.getResume().toLowerCase().contains(filterPatern)
                            || String.valueOf(book.getDate()).contains(filterPatern)
                            || genre.toLowerCase().contains(filterPatern)

                    ) {
                        fiteredList.add(book); // on ajoute les livres sinon
                    }
                    else{
                        book.bitmapVoid(); // pr evité d'utiliser trop de memoire
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = fiteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Liste.clear();
            Liste.addAll((Vector) results.values);
            notifyDataSetChanged();
        }
    };
    
}


