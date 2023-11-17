package com.example.findmybuga;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

    private ArrayList<PoiPos> listData;
    private static Context adapHolder;

    public MyListAdapter(ArrayList<PoiPos> listData) {
        // Carga el array de datos en el Adapter
        this.listData = listData;
    }

    public void addNewPos(ArrayList<PoiPos> lstData) {
        this.listData = lstData;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView posDesc;
        public TextView posLati;
        public TextView posLong;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Instancia la imagen en la celda
            this.posDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            this.posLati = (TextView) itemView.findViewById(R.id.txtLati);
            this.posLong = (TextView) itemView.findViewById(R.id.txtLong);
        }
    }

    @NonNull
    @Override
    public MyListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater lytInflater = LayoutInflater.from(parent.getContext());
        // Cargamos la celda creada previamente
        View lstItem = lytInflater.inflate(R.layout.pos_cell, parent, false);
        adapHolder = parent.getContext();
        ViewHolder vwHolder = new ViewHolder(lstItem);

        return vwHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyListAdapter.ViewHolder holder, int position) {
        int myPos = position;
        final PoiPos mListData = listData.get(myPos);
        holder.posDesc.setText(mListData.getDescription());
        holder.posLati.setText(mListData.getLati());
        holder.posLong.setText(mListData.getLong());

    }

    @Override
    public int getItemCount() {
        // Control de carga asincrona
        if (this.listData == null) {
            return 0;
        }
        // Retorna la cantidad de elementos del array
        return this.listData.size();
    }
}
