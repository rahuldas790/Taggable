package rahulkumardas.taggableapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul Kumar Das on 09-09-2017.
 */

public class Post implements Parcelable{

    public Post(String imageUrl, String name, List<String> tags) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.tags = tags;
        tags.add("No tags");
    }

    public Post() {
        tags.add("No tags");
    }

    protected Post(Parcel in) {
        imageUrl = in.readString();
        name = in.readString();
        tagId = in.readString();
        tags = in.createStringArrayList();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    private String imageUrl, name, postId;
    private String tagId;
    private List<String> tags = new ArrayList<>();

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imageUrl);
        parcel.writeString(name);
        parcel.writeString(tagId);
        parcel.writeStringList(tags);
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
