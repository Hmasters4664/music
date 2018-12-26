package com.example.hassani.music.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hassani.music.R;
import com.example.hassani.music.audioclass.audio;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder>  {

    public List<audio> aList;
    private Context mContext;


    public MusicAdapter( List<audio> aList)

    {
        this.aList=aList;

    }

    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.music_card,parent,false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // item clicked

            }
        });
        return new MusicAdapter.ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return aList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.sName.setText(aList.get(position).getTitle());
        holder.aArtist.setText(aList.get(position).getArtist());


    }

    public class  ViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public TextView sName;
        public TextView aArtist;
        public ViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            sName= (TextView) mView.findViewById(R.id.name);
            aArtist= (TextView) mView.findViewById(R.id.artist);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Context context = mView.getContext();
                    ////fill out on click
                }
            });


        }
    }
}



