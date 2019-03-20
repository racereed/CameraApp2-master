package camera1.themaestrochef.com.cameraappfordogs.Adapters;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import camera1.themaestrochef.com.cameraappfordogs.Models.AnimalsModel;
import camera1.themaestrochef.com.cameraappfordogs.R;
import camera1.themaestrochef.com.cameraappfordogs.Models.AnimalsModel;

import static android.content.Context.AUDIO_SERVICE;


public class SoundsAdapter extends RecyclerView.Adapter<SoundsAdapter.ViewHolder> {

    private ArrayList<AnimalsModel>animalsModels;
    private Context mcontext;
    private MediaPlayer mPlayer;

    public SoundsAdapter(ArrayList<AnimalsModel>animalsModels,Context mcontext){
        this.animalsModels=animalsModels;
        this.mcontext=mcontext;

    }
    @NonNull
    @Override
    public SoundsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row= LayoutInflater.from(parent.getContext()).inflate(R.layout.animal_item,parent,false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundsAdapter.ViewHolder holder, int position) {
        holder.animalImage.setImageResource(animalsModels.get(position).getImageRes());
    }

    @Override
    public int getItemCount() {
        if(animalsModels==null){
            return 0;
        }
        return animalsModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView animalImage;
        ViewHolder(View itemView) {
            super(itemView);
            animalImage=itemView.findViewById(R.id.animal_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mPlayer!=null&&mPlayer.isPlaying()){
                mPlayer.stop();
            }
            mPlayer = MediaPlayer.create(mcontext, animalsModels.get(getAdapterPosition()).getAudioRes());
            mPlayer.start();//Start playing the music
            makeVolumeToast();
        }
    }
    private void makeVolumeToast(){

        // Get the AudioManager instance
        AudioManager am = (AudioManager) mcontext.getSystemService(AUDIO_SERVICE);
        // Get the music current volume level
        int music_volume_level = 0;
        if (am != null) {
            music_volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        // Get the device music maximum volume level
        if (am != null) {
            int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }

        if (music_volume_level < 2){
            Toast.makeText(mcontext,"Volume Might Be Too Low",Toast.LENGTH_LONG).show();// Set your own toast  message
        }
    }
    public void onPauseScreen(){
        if (mPlayer!=null){
            mPlayer.stop();
        }
    }
}
