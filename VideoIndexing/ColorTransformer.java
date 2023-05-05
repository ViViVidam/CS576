package VideoIndexing;

public class ColorTransformer {
    private static final double CIE_EPSILON = 216.0 / 24389.0;
    /**
     * Constant for the CIE XYZ and CIE L*u*v* color spaces: (29/3)^3 *
     */
    private static final double CIE_KAPPA = 24389.0 / 27.0;
    /**
     * Xr, Yr, Zr constants with D50 white point used for CIE XYZ to
     * CIE L*u*v* conversion *
     */
    private static final double[] XYZ_R_D50 = { 0.964221, 1.000000, 0.825211 };
    /**
     * sRGB to CIE XYZ conversion matrix. See
     * http://www.brucelindbloom.com/index.html?WorkingSpaceInfo.html#Specifications
     * *
     */
    private static final double[] MATRIX_SRGB2XYZ_D50 = { 0.436052025, 0.385081593, 0.143087414, 0.222491598,
            0.716886060, 0.060621486, 0.013929122, 0.097097002, 0.714185470 };
    public static void rgb2luv(int R, int G, int B, int []luv) {
        //http://www.brucelindbloom.com

        double r, g, b, X, Y, Z, yr;
        double L;
        double eps = 216./24389.;
        double k = 24389./27.;

        double Xr = 0.964221;  // reference white D50
        double Yr = 1.0;
        double Zr = 0.825211;


//			double ur=4.*.964221/(.964221+15.+3.*.825211);
//			// vr=9*Yr/(Xr+15*Yr+3*Zr)
//			double vr=9./(.964221+15.+3.*.825211);

//			double dmax=(double)255;
//			double dmaxdiv2=(double)(255./2.+1);

        // multiplication is faster than division
        double div12p92=1./12.92;
        double div1p055=1./1.055;
//			double aThird=1./3.;
        double divmax=1./(double)255;

        // RGB to XYZ

        r = R*divmax; //R 0..1
        g = G*divmax; //G 0..1
        b = B*divmax; //B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r *=div12p92;
        else
            r = (float) Math.pow((r+0.055)*div1p055,2.4);

        if (g <= 0.04045)
            g *=div12p92;
        else
            g = (float) Math.pow((g+0.055)*div1p055,2.4);

        if (b <= 0.04045)
            b *=div12p92;
        else
            b = (float) Math.pow((b+0.055)*div1p055,2.4);

			/*
			 X_ =  0.412424f * r + 0.357579f * g + 0.180464f  * b;
			 Y_ =  0.212656f * r + 0.715158f * g + 0.0721856f * b;
			 Z_ = 0.0193324f * r + 0.119193f * g + 0.950444f  * b;

			 // chromatic adaptation transform from D65 to D50
			  X =  1.047835f * X_ + 0.022897f * Y_ - 0.050147f * Z_;
			  Y =  0.029556f * X_ + 0.990481f * Y_ - 0.017056f * Z_;
			  Z = -0.009238f * X_ + 0.015050f * Y_ + 0.752034f * Z_;
			 */

        X =  0.436052025f*r	+ 0.385081593f*g + 0.143087414f *b;
        Y =  0.222491598f*r	+ 0.71688606f *g + 0.060621486f *b;
        Z =  0.013929122f*r	+ 0.097097002f*g + 0.71418547f  *b;

        // XYZ to Luv

        double u, v, u_, v_, ur_, vr_;

        u_ = 4*X / (X + 15*Y + 3*Z);
        v_ = 9*Y / (X + 15*Y + 3*Z);

        ur_ = 4*Xr / (Xr + 15*Yr + 3*Zr);
        vr_ = 9*Yr / (Xr + 15*Yr + 3*Zr);

        yr = Y/Yr;

        if ( yr > eps )
            L =  (float) (116*Math.pow(yr, 1/3.) - 16);
        else
            L = k * yr;

        u = 13*L*(u_ -ur_);
        v = 13*L*(v_ -vr_);

        luv[2] = (int) (2.55*L + .5);
        luv[0] = (int) (u + .5);
        luv[1] = (int) (v + .5);
    }

    /**
     * Converts color components from the sRGB to the CIE XYZ color space.
     * A D50 white point is assumed for the sRGB conversion. If the <i>xyz</i>
     * array is {@code null}, a new one will be created with the same
     * size as the <i>rgb</i> array.
     *
     * See http://www.brucelindbloom.com/index.html?Eqn_RGB_to_XYZ.html
     *
     * @param rgb Color components in the sRGB color space.
     * @param xyz Optional array to store color components in the CIE XYZ color
     * space.
     * @return Color components in the CIE XYZ color space.
     */
    public static double[] rgb2xyz(double[] rgb, double[] xyz) {
        if (xyz == null) {
            xyz = new double[rgb.length];
        }

        // Remove sRGB companding to make RGB components linear
        double[] rgbLin = new double[rgb.length];
        for (int i = 0; i < rgb.length; i++) {
            if (rgb[i] <= 0.04045) {
                rgbLin[i] = rgb[i] / 12.92;
            } else {
                rgbLin[i] = Math.pow((rgb[i] + 0.055) / 1.055, 2.4);
            }
        }

        // Convert linear sRGB with D50 white point to CIE XYZ
        for (int i = 0; i < xyz.length; i++) {
            xyz[i] = MATRIX_SRGB2XYZ_D50[i * 3 + 0] * rgbLin[0] + MATRIX_SRGB2XYZ_D50[i * 3 + 1] * rgbLin[1]
                    + MATRIX_SRGB2XYZ_D50[i * 3 + 2] * rgbLin[2];
        }

        return xyz;
    }

    /**
     * Converts color components from the CIE XYZ to the CIE L*u*v* color
     * space. If the <i>luv</i> array is {@code null}, a new one will be
     * created with the same size as the <i>xyz</i> array.
     *
     * http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Luv.html
     *
     * @param xyz Color components in the CIE XYZ color space.
     * @param luv Optional array for storing color components in the CIE L*u*v*
     * color space.
     * @return Color components in the CIE L*u*v* color space.
     */
    public static double[] xyz2luv(double[] xyz, double[] luv) {
        double tmp = xyz[0] + 15.0 * xyz[1] + 3.0 * xyz[2];
        if (tmp == 0.0) {
            tmp = 1.0;
        }
        double u1 = 4.0 * xyz[0] / tmp;
        double v1 = 9.0 * xyz[1] / tmp;

        // Relative luminance
        double yr = xyz[1] / XYZ_R_D50[1];
        double ur = 4.0 * XYZ_R_D50[0] / (XYZ_R_D50[0] + 15.0 * XYZ_R_D50[1] + 3.0 * XYZ_R_D50[2]);
        double vr = 9.0 * XYZ_R_D50[1] / (XYZ_R_D50[0] + 15.0 * XYZ_R_D50[1] + 3.0 * XYZ_R_D50[2]);

        // Mapping relative luminance to lightness
        if (luv == null) {
            luv = new double[xyz.length];
        }
        if (yr > CIE_EPSILON) {
            luv[0] = 116.0 * Math.pow(yr, 1.0 / 3.0) - 16.0;
        } else {
            luv[0] = CIE_KAPPA * yr;
        }
        luv[1] = 13.0 * luv[0] * (u1 - ur);
        luv[2] = 13.0 * luv[0] * (v1 - vr);

        return luv;
    }
}
