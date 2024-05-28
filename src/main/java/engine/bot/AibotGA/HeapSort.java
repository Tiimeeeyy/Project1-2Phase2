package engine.bot.AibotGA;
/*
 * @(#)HeapSortAlgorithm.java   1.0 95/06/23 Jason Harrison
 *
 * Copyright (c) 1995 University of British Columbia
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies. Please refer to the file "copyright.html"
 * for further important copyright and licensing information.
 *
 * UBC MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. UBC SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

/**
 * A heap sort demonstration algorithm
 * SortAlgorithm.java, Thu Oct 27 10:32:35 1994
 *
 * Modified by Steven de Jong for Genetic Algorithms.
 * 
 * Modified by Jo Stevens for practical session.
 *
 *
 * @author Jason Harrison@cs.ubc.ca
 * @version     1.0, 23 Jun 1995
 *
 * @author Steven de Jong
 * @version     1.1, 08 Oct 2004
 * 
 * @author Jo Stevens
 * @version 1.2, 14 Nov 2008
 * 
 */
public class HeapSort 
{
    /**
     * The sort method sorts an array of individuals in-place using the heap sort algorithm.
     * @param i The array of individuals to be sorted.
     */
    public static void sort(Individual i[])
    {
        int N = i.length;
        // Build the heap
        for (int k = N/2; k > 0; k--) 
        downheap(i, k, N);
        // Sort the array
        do 
        {
            Individual T = i[0];
            i[0] = i[N - 1];
            i[N - 1] = T;
            
            N = N - 1;
            downheap(i, 1, N);
        } 
        while (N > 1);
    }

    /**
     * The downheap method is a helper function that ensures the heap property is maintained for the element at index k.
     * @param i The array of individuals.
     * @param k The index of the element to downheap.
     * @param N The size of the heap.
     */
    private static void downheap(Individual i[], int k, int N)
    {
        Individual T = i[k - 1];
        
        while (k <= N/2) 
        {
            int j = k + k;
            if ((j < N) && (i[j - 1].getFitness() > i[j].getFitness())) 
            j++;

            if (T.getFitness() <= i[j - 1].getFitness()) 
            break;

            else 
            {
                i[k - 1] = i[j - 1];
                k = j;
            }
        }
        i[k - 1] = T;
    }
}

