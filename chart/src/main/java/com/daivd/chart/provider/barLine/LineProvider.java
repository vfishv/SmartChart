package com.daivd.chart.provider.barLine;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

import com.daivd.chart.data.LineData;
import com.daivd.chart.data.style.LineStyle;
import com.daivd.chart.data.style.PointStyle;
import com.daivd.chart.provider.component.line.BrokenLineModel;
import com.daivd.chart.provider.component.line.ILineModel;
import com.daivd.chart.provider.component.point.IPoint;
import com.daivd.chart.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 线内容绘制
 * Created by huang on 2017/9/26.
 */

public class LineProvider extends BaseBarLineProvider<LineData> {

    private LineStyle lineStyle = new LineStyle();
    private IPoint point;
    private int rowSize;
    private boolean isArea; //是否打开面积图
    private boolean isDrawLine = true;
    private ILineModel lineModel = new BrokenLineModel();

    @Override
    public void drawProvider(Canvas canvas, Rect zoomRect, Rect rect, Paint paint) {

        List<LineData> columnDataList = chartData.getColumnDataList();
        int columnSize = columnDataList.size();
        rowSize = chartData.getCharXDataList().size();
        for (int i = 0; i < columnSize; i++) {
            LineData columnData = columnDataList.get(i);
            paint.setColor(columnData.getColor());
            if (!columnData.isDraw()) {
                continue;
            }
            List<Float> pointX = new ArrayList<>();
            List<Float> pointY = new ArrayList<>();
            List<Double> chartYDataList = columnData.getChartYDataList();
            for (int j = 0; j < rowSize; j++) {
                double value = chartYDataList.get(j);
                float x = getStartX(zoomRect, j);
                float y = getStartY(zoomRect, value, columnData.getDirection());
                pointX.add(x);
                pointY.add(y);
                drawPointText(canvas, value, x, y, paint);
            }
            lineStyle.fillPaint(paint);
            paint.setColor(columnData.getColor());
            canvas.save();
            canvas.clipRect(rect);
            drawLine(canvas, rect, pointX, pointY, paint);
            drawTip(canvas, pointX, pointY, columnData);
            canvas.restore();
            drawPoint(canvas, pointX, pointY, paint);

        }
        drawLevelLine(canvas, zoomRect, paint);
        drawClickCross(canvas, rect, zoomRect, paint);
    }

    private void drawLine(Canvas canvas, Rect rect, List<Float> pointX, List<Float> pointY, Paint paint) {
        if (isDrawLine) {
            Path path = lineModel.getLinePath(pointX, pointY);
            if (isArea) {
                paint.setStyle(Paint.Style.FILL);
                path.lineTo(rect.right, rect.bottom);
                path.lineTo(rect.left, rect.bottom);
                path.close();
                int alphaColor = ColorUtils.changeAlpha(paint.getColor(), 125);
                paint.setColor(alphaColor);
            }
            canvas.drawPath(path, paint);
        }
    }

    @Override
    protected void matrixRectEnd(Canvas canvas, Rect rect) {

    }

    @Override
    protected void matrixRectStart(Canvas canvas, Rect rect) {

    }

    private float getStartX(Rect zoomRect, int position) {

        double width = zoomRect.width() / (rowSize - 1);
        return (float) (position * width + zoomRect.left);
    }


    private void drawClickCross(Canvas canvas, Rect rect, Rect zoomRect, Paint paint) {

        if (pointF != null && (isOpenCross() || isOpenMark())) {
            int minPositionX = 0, minPositionY = 0;
            float centerX = 0, centerY = 0;
            LineData clickLineData = null;
            float minDisX = zoomRect.width();
            for (int i = 0; i < rowSize; i++) {
                float startX = getStartX(zoomRect, i);
                float disX = Math.abs(pointF.x - startX);
                if (disX < minDisX) {
                    minPositionX = i;
                    minDisX = disX;
                    centerX = startX;
                } else {
                    break;
                }
            }
            List<LineData> lineDates = chartData.getColumnDataList();
            float minDisY = zoomRect.height();
            boolean isHasLine = false;
            for (int j = 0; j < lineDates.size(); j++) {
                LineData lineData = lineDates.get(j);
                if (!lineData.isDraw()) {
                    continue;
                }
                isHasLine = true;
                double value = lineData.getChartYDataList().get(minPositionX);
                float startY = getStartY(zoomRect, value, lineData.getDirection());
                float disY = Math.abs(pointF.y - startY);
                if (disY < minDisY) {
                    centerY = startY;
                    minDisY = disY;
                    clickLineData = lineData;
                    minPositionY = j;
                }
            }
            if (!isHasLine || !containsRect(centerX, centerY)) {
                return;
            }
            if (onClickColumnListener != null) {
                onClickColumnListener.onClickColumn(clickLineData, minPositionX);
            }
            if (isOpenCross() && getCross() != null) {
                getCross().drawCross(canvas, new PointF(centerX, centerY), rect, paint);
            }
            drawMark(canvas, centerX, centerY, rect, minPositionX, minPositionY);
        }
    }

    private void drawPoint(Canvas canvas, List<Float> pointX, List<Float> pointY, Paint paint) {
        if (point != null) {
            for (int i = 0; i < pointY.size(); i++) {
                float x = pointX.get(i);
                float y = pointY.get(i);
                if (containsRect(x, y)) {
                    point.drawPoint(canvas, x, y, false,paint);
                }
            }
        }
    }

    private void drawTip(Canvas canvas, List<Float> pointX, List<Float> pointY, LineData lineData) {
        if (tip != null) {
            for (int i = 0; i < pointY.size(); i++) {
                float x = pointX.get(i);
                float y = pointY.get(i);
                drawTip(canvas, x, y, lineData, i);
            }
        }
    }


    public LineStyle getLineStyle() {
        return lineStyle;
    }


    @Override
    public double[] setMaxMinValue(double maxValue, double minValue) {
        double dis = Math.abs(maxValue - minValue);
        maxValue = maxValue + dis * 0.3;
        if (minValue > 0) {
            minValue = 0;
        } else {
            minValue = minValue - dis * 0.3;
        }
        return new double[]{maxValue, minValue};
    }


    public void setArea(boolean isArea) {
        this.isArea = isArea;
    }

    public void setLineModel(ILineModel lineModel) {
        this.lineModel = lineModel;
    }

    public void setDrawLine(boolean drawLine) {
        isDrawLine = drawLine;
    }


    public void setPoint(IPoint point) {
        this.point = point;
    }
}
