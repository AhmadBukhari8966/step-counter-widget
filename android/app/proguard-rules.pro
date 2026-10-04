# Glance creates widget button callbacks (like RefreshAction) by reflection from their
# class name, so R8 must keep their no-argument constructors.
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    <init>();
}
