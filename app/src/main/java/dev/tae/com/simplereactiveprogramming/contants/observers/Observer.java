package dev.tae.com.simplereactiveprogramming.contants.observers;

import dev.tae.com.simplereactiveprogramming.model.Model;
import retrofit.http.GET;
import rx.Observable;

/**
 * Created by TAE on 15/06/2016.
 */
public interface Observer {
    @GET("/?q=simpsons+characters&format=json")
    Observable<Model> fetchData();

}
