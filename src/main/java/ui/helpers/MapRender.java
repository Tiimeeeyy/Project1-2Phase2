// package ui.helpers;

// import engine.solvers.Utility;
// import javafx.scene.paint.Color;

// public class MapRender {
//     private Color getModifiedColor(double height) {
//         if (height < MIN_HEIGHT || height > MAX_HEIGHT) {
//             throw new Error("Out of range functions");
//         } else {
//             int gr = Utility.heightToColor(height);
//             gr = Math.max(0, Math.min(255, gr));

//             if (height < 0) {
//                 if (disableWater) {
//                     return Color.rgb(0, gr, 0); // Green color for negative height when water is disabled
//                 } else {
//                     return Color.rgb(0, gr, 150); // Blue color for negative height when water is enabled
//                 }
//             } else {
//                 return Color.rgb(0, gr, 0);
//             }
//         }
//     }
// }
