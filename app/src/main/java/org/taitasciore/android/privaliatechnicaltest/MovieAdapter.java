package org.taitasciore.android.privaliatechnicaltest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 06/03/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private static String BASE_URL_IMG = "https://image.tmdb.org/t/p/";
    private static final String BASE_URL_IMG_PHONE = BASE_URL_IMG + "w500/";
    private static final String BASE_URL_IMG_TABLET = BASE_URL_IMG + "w780/";

    Activity context;
    ArrayList<MovieResponse.Movie> list;

    public MovieAdapter(Activity context, ArrayList<MovieResponse.Movie> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_row_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MovieResponse.Movie m = list.get(position);
        holder.title.setText(buildSpan(m.getTitle(), getYear(m.getReleaseDate())),
                TextView.BufferType.SPANNABLE);
        holder.overview.setText(m.getOverview());
        Uri uri = Uri.parse(BASE_URL_IMG_PHONE + m.getPosterPath());
        holder.img.setImageURI(uri);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void add(MovieResponse.Movie movie) {
        list.add(movie);
        notifyItemInserted(getItemCount());
    }

    public ArrayList<MovieResponse.Movie> getList() {
        return list;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    /**
     * This metod returns the year of release of a movie, given the complete release date
     * in YYYY-MM-DD format. It splits the string, separating by the '-' character,
     * taking only the first element of the returned array (the year)
     * @param date Release date of the movie
     * @return Year of release of the movie
     */
    public String getYear(String date) {
        return date.split("-")[0];
    }

    /**
     * This method returns a span.
     * If year is available, it will change the font to OpenSans-Light and
     * reduce the font size a bit to display the year
     * Else, returns span without modifications, showing only the movie title
     * @param title Movie title
     * @param year Year of release
     * @return Span
     */
    public SpannableStringBuilder buildSpan(String title, String year) {
        if (!year.isEmpty()) {
            String fullStr = title + " (" + year + ")";
            SpannableStringBuilder ssb = new SpannableStringBuilder(fullStr);
            CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(
                    TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Light.ttf"));
            RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.8f);
            int start = fullStr.lastIndexOf(" ");
            int end = fullStr.length();
            ssb.setSpan(typefaceSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            ssb.setSpan(sizeSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            return ssb;
        }
        else
            return new SpannableStringBuilder(title);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_title) TextView title;

        @BindView(R.id.tv_overview) TextView overview;
        @BindView(R.id.drawee_view) SimpleDraweeView img;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
