package ui;
// package ui;

// import org.jzy3d.chart.AWTChart;
// import org.jzy3d.chart.factories.AWTChartComponentFactory;
// import org.jzy3d.colors.Color;
// import org.jzy3d.colors.colormaps.ColorMapRainbow;
// // import org.jzy3d.colors.mapper.ColorMapper;
// import org.jzy3d.maths.Range;
// import org.jzy3d.plot3d.builder.Builder;
// import org.jzy3d.plot3d.builder.Mapper;
// import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
// import org.jzy3d.plot3d.primitives.Shape;
// import org.jzy3d.plot3d.rendering.canvas.Quality;

// public class HeightFunctionJzy3d {
//     public static void main(String[] args) {
//         // Define the function to plot
//         Mapper mapper = new Mapper() {
//             @Override
//             public double f(double x, double y) {
//                 return Math.sin(Math.sqrt(x * x + y * y));
//             }
//         };

//         // Define the range and step
//         Range range = new Range(-3, 3);
//         int steps = 80;

//         // Create the shape
//         Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps), mapper);
//         surface.setColorMapper(new org.jzy3d.colors.ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax()));
//         surface.setFaceDisplayed(true);
//         surface.setWireframeDisplayed(false);

//         // Create a chart and add the surface
//         AWTChart chart = (AWTChart) AWTChartComponentFactory.chart(Quality.Advanced, "newt");
//         chart.getScene().getGraph().add(surface);
//         chart.open("Jzy3d Example", 800, 600);
//     }
// }