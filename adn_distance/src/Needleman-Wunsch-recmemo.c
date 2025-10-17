/**
 * \file Needleman-Wunsch-recmemo.c
 * \brief recursive implementation with memoization of Needleman-Wunsch global alignment algorithm that computes the distance between two genetic sequences
 * \version 0.1
 * \date 03/10/2022
 * \author Jean-Louis Roch (Ensimag, Grenoble-INP - University Grenoble-Alpes) jean-louis.roch@grenoble-inp.fr
 *
 * Documentation: see Needleman-Wunsch-recmemo.h
 * Costs of basic base opertaions (SUBSTITUTION_COST, SUBSTITUTION_UNKNOWN_COST, INSERTION_COST) are
 * defined in Needleman-Wunsch-recmemo.h
 */

#include "Needleman-Wunsch-recmemo.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h> /* for strchr */
// #include <ctype.h> /* for toupper */

#include "characters_to_base.h" /* mapping from char to base */

/*****************************************************************************/

/* Context of the memoization : passed to all recursive calls */
/** \def NOT_YET_COMPUTED
 * \brief default value for memoization of minimal distance (defined as an impossible value for a distance, -1).
 */
#define NOT_YET_COMPUTED -1L

/** \struct NW_MemoContext
 * \brief data for memoization of recursive Needleman-Wunsch algorithm
 */
struct NW_MemoContext
{
   char *X;     /*!< the longest genetic sequences */
   char *Y;     /*!< the shortest genetic sequences */
   size_t M;    /*!< length of X */
   size_t N;    /*!< length of Y,  N <= M */
   long **memo; /*!< memoization table to store memo[0..M][0..N] (including stopping conditions phi(M,j) and phi(i,N) */
};

/*
 *  static long EditDistance_NW_RecMemo(struct NW_MemoContext *c, size_t i, size_t j)
 * \brief  EditDistance_NW_RecMemo :  Private (static)  recursive function with memoization \
 * direct implementation of Needleman-Wursch extended to manage FASTA sequences (cf TP description)
 * \param c : data passed for recursive calls that includes the memoization array
 * \param i : starting position of the left sequence :  c->X[ i .. c->M ]
 * \param j : starting position of the right sequence :  c->Y[ j .. c->N ]
 */
static long EditDistance_NW_RecMemo(struct NW_MemoContext *c, size_t i, size_t j)
/* compute and returns phi(i,j) using data in c -allocated and initialized by EditDistance_NW_Rec */
{
   if (c->memo[i][j] == NOT_YET_COMPUTED)
   {
      long res;
      char Xi = c->X[i];
      char Yj = c->Y[j];
      if (i == c->M) /* Reach end of X */
      {
         if (j == c->N)
            res = 0; /* Reach end of Y too */
         else
            res = (isBase(Yj) ? INSERTION_COST : 0) + EditDistance_NW_RecMemo(c, i, j + 1);
      }
      else if (j == c->N) /* Reach end of Y but not end of X */
      {
         res = (isBase(Xi) ? INSERTION_COST : 0) + EditDistance_NW_RecMemo(c, i + 1, j);
      }
      else if (!isBase(Xi)) /* skip ccharacter in Xi that is not a base */
      {
         ManageBaseError(Xi);
         res = EditDistance_NW_RecMemo(c, i + 1, j);
      }
      else if (!isBase(Yj)) /* skip ccharacter in Yj that is not a base */
      {
         ManageBaseError(Yj);
         res = EditDistance_NW_RecMemo(c, i, j + 1);
      }
      else
      {             /* Note that stopping conditions (i==M) and (j==N) are already stored in c->memo (cf EditDistance_NW_Rec) */
         long min = /* initialization  with cas 1*/
             (isUnknownBase(Xi) ? SUBSTITUTION_UNKNOWN_COST
                                : (isSameBase(Xi, Yj) ? 0 : SUBSTITUTION_COST)) +
             EditDistance_NW_RecMemo(c, i + 1, j + 1);
         {
            long cas2 = INSERTION_COST + EditDistance_NW_RecMemo(c, i + 1, j);
            if (cas2 < min)
               min = cas2;
         }
         {
            long cas3 = INSERTION_COST + EditDistance_NW_RecMemo(c, i, j + 1);
            if (cas3 < min)
               min = cas3;
         }
         res = min;
      }
      c->memo[i][j] = res;
   }
   return c->memo[i][j];
}

/* EditDistance_NW_Rec :  is the main function to call, cf .h for specification
 * It allocates and initailizes data (NW_MemoContext) for memoization and call the
 * recursivefunction EditDistance_NW_RecMemo
 * See .h file for documentation
 */
long EditDistance_NW_Rec(char *A, size_t lengthA, char *B, size_t lengthB)
{
   _init_base_match();
   struct NW_MemoContext ctx;
   if (lengthA >= lengthB) /* X is the longest sequence, Y the shortest */
   {
      ctx.X = A;
      ctx.M = lengthA;
      ctx.Y = B;
      ctx.N = lengthB;
   }
   else
   {
      ctx.X = B;
      ctx.M = lengthB;
      ctx.Y = A;
      ctx.N = lengthA;
   }
   size_t M = ctx.M;
   size_t N = ctx.N;
   { /* Allocation and initialization of ctx.memo to NOT_YET_COMPUTED*/
      /* Note: memo is of size (N+1)*(M+1) but is stored as (M+1) distinct arrays each with (N+1) continuous elements
       * It would have been possible to allocate only one big array memezone of (M+1)*(N+1) elements
       * and then memo as an array of (M+1) pointers, the memo[i] being the address of memzone[i*(N+1)].
       */
      ctx.memo = (long **)malloc((M + 1) * sizeof(long *));
      if (ctx.memo == NULL)
      {
         perror("EditDistance_NW_Rec: malloc of ctx_memo");
         exit(EXIT_FAILURE);
      }
      for (int i = 0; i <= M; ++i)
      {
         ctx.memo[i] = (long *)malloc((N + 1) * sizeof(long));
         if (ctx.memo[i] == NULL)
         {
            perror("EditDistance_NW_Rec: malloc of ctx_memo[i]");
            exit(EXIT_FAILURE);
         }
         for (int j = 0; j <= N; ++j)
            ctx.memo[i][j] = NOT_YET_COMPUTED;
      }
   }

   /* Compute phi(0,0) = ctx.memo[0][0] by calling the recursive function EditDistance_NW_RecMemo */
   long res = EditDistance_NW_RecMemo(&ctx, 0, 0);

   { /* Deallocation of ctx.memo */
      for (int i = 0; i <= M; ++i)
         free(ctx.memo[i]);
      free(ctx.memo);
   }
   return res;
}


long NW_itera(char *X, size_t tailleX, char *Y, size_t tailleY)
{
   if (tailleX < tailleY)
   {
      size_t saveX = tailleX;
      char *savecharX = X;
      X = Y;
      tailleX = tailleY;
      tailleY = saveX;
      Y = savecharX;
   }

   _init_base_match();
   long prev_diago = 0;
   long *tab_curr = malloc(sizeof(long) * (tailleX + 1));
   for (int i = 0; i <= tailleX; i++)
   {
      tab_curr[i] = i * INSERTION_COST;
   }

   for (int j = 1; j <= tailleY; j++)
   {
      tab_curr[0] = j * INSERTION_COST;
      for (int i = 1; i <= tailleX; i++)
      {
         if (!isBase(X[i - 1])) /* skip ccharacter in Xi that is not a base */
         {
            ManageBaseError(X[i - 1]);
            tab_curr[i] = tab_curr[i - 1];
            continue;
         }
         else if (!isBase(Y[j - 1])) /* skip ccharacter in Yj that is not a base */
         {
            ManageBaseError(Y[j - 1]);
            continue;
         }

         else
         {             /* Note that stopping conditions (i==M) and (j==N) are already stored in c->memo (cf EditDistance_NW_Rec) */
            long min = /* initialization with cas 1*/
                (isUnknownBase(X[i - 1]) ? SUBSTITUTION_UNKNOWN_COST
                                         : (isSameBase(X[i - 1], Y[j - 1]) ? 0 : SUBSTITUTION_COST)) +
                prev_diago;
            {
               long cas2 = INSERTION_COST + tab_curr[i];
               if (cas2 < min)
                  min = cas2;
            }
            {
               long cas3 = INSERTION_COST + tab_curr[i - 1];
               if (cas3 < min)
                  min = cas3;
            }
            prev_diago = tab_curr[i];
            tab_curr[i] = min;
         }
      }
   }
   long result = tab_curr[tailleX];
   free(tab_curr);
   return result;
}


long NW_itera_cache_aware(char *X, size_t tailleX, char *Y, size_t tailleY, size_t K)
{
   if (tailleX < tailleY)
   {
      size_t saveX = tailleX;
      char *savecharX = X;
      X = Y;
      tailleX = tailleY;
      tailleY = saveX;
      Y = savecharX;
   }

   _init_base_match();
   long *tab_curr = malloc(sizeof(long) * (tailleX + 1));
   long *memo_col = malloc(sizeof(long) * (tailleY + 1));
   int memo_idx = 1;
   long prev_horiz = 0;
   long prev_diago = 0;
   long prev_mem_col = 0;
   for (int i = 0; i <= tailleX; i++)
   {
      tab_curr[i] = i * INSERTION_COST;
   }

   for (int j = 0; j <= tailleY; j++)
   {
      memo_col[j] = j * INSERTION_COST;
   }

   for (int ii = 1; ii <= tailleX; ii += K)
   {
      memo_idx = 1;
      for (int jj = 1; jj <= tailleY; jj += K)
      {
         {
            // Process sub-block of size K x K
            for (int j = jj; j < jj + K && j <= tailleY; j++)
            {
               for (int i = ii; i < ii + K && i <= tailleX; i++)

               {
                  if (i == ii)
                  {
                     prev_horiz = memo_col[j];
                     prev_diago = prev_mem_col;
                  }
                  else
                  {
                     prev_horiz = tab_curr[i - 1];
                  }
                  if (!isBase(X[i - 1]))
                  {
                     ManageBaseError(X[i - 1]);
                     tab_curr[i] = prev_horiz;
                  }
                  else if (!isBase(Y[j - 1]))
                  {
                     ManageBaseError(Y[j - 1]);
                  }
                  else
                  {
                     long min =
                         (isUnknownBase(X[i - 1]) ? SUBSTITUTION_UNKNOWN_COST
                                                  : (isSameBase(X[i - 1], Y[j - 1]) ? 0 : SUBSTITUTION_COST)) +
                         prev_diago;

                     long cas2 = INSERTION_COST + tab_curr[i];
                     if (cas2 < min)
                        min = cas2;

                     long cas3 = INSERTION_COST + prev_horiz;
                     if (cas3 < min)
                        min = cas3;
                     prev_diago = tab_curr[i];
                     tab_curr[i] = min;
                  }

                  if (i == (ii + K - 1) || i == tailleX)
                  {
                     prev_mem_col = memo_col[j];
                     memo_col[memo_idx++] = tab_curr[i];
                  }
               }
            }
         }
      }
   }

   long result = tab_curr[tailleX];
   free(tab_curr);
   free(memo_col);
   return result;
}

long NW_cache_oblivious_aux(char *X, size_t tailleX, char *Y, size_t tailleY, size_t startX, size_t endX, long *tab, long *prev_diago, long *mem_col, size_t S)
{
   // Cas de base : petite zone 1x1 ou SxN, on calcule directement
   if (endX - startX <= S)
   {
      // Effectuer directement sur tab
      for (size_t j = 1; j <= tailleY; j++)
      {
         for (size_t i = startX; i <= endX; i++)
         {
            if (j == 1)
            {
               *prev_diago = INSERTION_COST * (i - 1);
            }
            if (i == 0)
            {
               tab[i] = j * INSERTION_COST;
               if (i == endX)
               {
                  mem_col[j] = tab[i];
               }
               continue;
            }
            if (i == startX)
            {
               *prev_diago = mem_col[j];
            }
            // Calcule similaire à la boucle de base de NW_itera_cache_aware
            if (!isBase(X[i - 1]))
            {
               ManageBaseError(X[i - 1]);
               tab[i] = tab[i - 1];
            }
            else if (!isBase(Y[j - 1]))
            {
               // On ne fait rien, on garde la valeur précédente
               ManageBaseError(Y[j - 1]);
            }
            else
            {
               long min = (isUnknownBase(X[i - 1]) ? SUBSTITUTION_UNKNOWN_COST : (isSameBase(X[i - 1], Y[j - 1]) ? 0 : SUBSTITUTION_COST)) + *prev_diago;

               long cas2 = INSERTION_COST + tab[i - 1];
               if (cas2 < min)
                  min = cas2;

               long cas3 = INSERTION_COST + tab[i];
               if (cas3 < min)
                  min = cas3;

               *prev_diago = tab[i];
               tab[i] = min;
               if (i == endX)
               {
                  mem_col[j] = tab[i];
               }
            }
         }
      }
      return tab[endX];
   }

   // Division des problèmes en 2 sous-problèmes pour la récursivité
   size_t midX = (startX + endX) / 2;

   NW_cache_oblivious_aux(X, tailleX, Y, tailleY, startX, midX, tab, prev_diago, mem_col, S);   // Gauche
   NW_cache_oblivious_aux(X, tailleX, Y, tailleY, midX + 1, endX, tab, prev_diago, mem_col, S); // Droit

   return tab[endX];
}

long NW_cache_oblivious(char *X, size_t tailleX, char *Y, size_t tailleY, size_t S)
{
   if (tailleX < tailleY)
   {
      size_t saveX = tailleX;
      char *savecharX = X;
      X = Y;
      tailleX = tailleY;
      tailleY = saveX;
      Y = savecharX;
   }

   _init_base_match();

   long *tab = malloc((tailleX + 1) * sizeof(long));

   for (size_t i = 0; i <= tailleX; i++)
   {
      tab[i] = i * INSERTION_COST;
   }
   long *mem_col = malloc((tailleY + 1) * sizeof(long));
   long prev_diago = 0;

   long result = NW_cache_oblivious_aux(X, tailleX, Y, tailleY, 0, tailleX, tab, &prev_diago, mem_col, S);

   free(tab);

   return result;
}