package vietnam.hu.com.androidapp;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

/**
 * Created by Pascal on 11/5/2017.
 *
 *
 *
 *
 *
 * This is not used anymore, left in place if statistics page is ever needed again.
 */

public class StatisticsActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Statistics");

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.content_statistics);
        View inflated = stub.inflate();

        GraphView graph = (GraphView) findViewById(R.id.graph);
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {
                new DataPoint(1, 50),
                new DataPoint(2, 120),
                new DataPoint(3, 150),
                new DataPoint(4, 260),
                new DataPoint(5, 200),
                new DataPoint(6, 180),
                new DataPoint(7, 150)
        });
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graph.addSeries(series);
    }
}
