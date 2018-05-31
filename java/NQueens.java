
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;


public class NQueens {
	private static long mask;
	private static BigInteger maskBigInteger;

	public static void main(String[] args) {
		BigInteger tmp = BigInteger.valueOf(1);
		System.out.println(tmp);
		System.out.println(BigInteger.ONE.not().and(BigInteger.valueOf(255)));

		testAllApproaches(8);
		testOneApproach(8, 14);
	}


	public static long countSolutionsWithBitOp(int n) {
		mask = (1 << n) - 1;

		return checkBits(0, 0, 0);
	}

	private static long checkBits(long row, long ld, long rd) {
		if (row == mask) {
			return 1;
		}

		long sum = 0;
		long pos = ~(row | ld | rd) & mask;
		while (pos != 0) {
			long p = pos & -pos;
			pos = pos & ~p;
			sum += checkBits(row | p, (ld | p) << 1, (rd | p) >>> 1);
		}

		return sum;
	}


	public static long countSolutionsWithBigInteger(int n) {
		maskBigInteger = BigInteger.ONE.shiftLeft(n).subtract(BigInteger.ONE);

		return checkBigInteger(BigInteger.valueOf(0), BigInteger.valueOf(0), BigInteger.valueOf(0));
	}


	private static long checkBigInteger(BigInteger row, BigInteger ld, BigInteger rd) {
		if (row.equals(maskBigInteger)) {
			return 1;
		}

		long sum = 0;

		BigInteger pos = row.or(ld).or(rd).not().and(maskBigInteger);
		while (!pos.equals(BigInteger.ZERO)) {
			BigInteger p = pos.and(pos.negate());
			pos = pos.and(p.not());
			sum += checkBigInteger(row.or(p), ld.or(p).shiftLeft(1), rd.or(p).shiftRight(1));
		}

		return sum;
	}


	public static long countSolutions(int n) {
		int[] board = new int[n];

		return placeQueenInRow(board, 0);
	}


	// board[x]=y denotes placing a queen in row x and column y
	private static long placeQueenInRow(int[] board, int row) {
		if (row == board.length) {
			return 1;
		}

		long count = 0;

		if (row == 0) { // first queen
			// chessboard is symmetrical
			for (int i = 0; i < (board.length + 1) / 2; i++) {
				board[row] = i;
				long result = placeQueenInRow(board, row + 1);
				if (board.length % 2 == 1 && i == board.length / 2) {
					count += result;
				} else {
					count += 2 * result;
				}
			}
		} else {
			for (int i = 0; i < board.length; i++) {
				if (isSafe(board, row, i)) {
					board[row] = i;
					count += placeQueenInRow(board, row + 1);
				}
			}
		}

		return count;
	}


	private static boolean isSafe(int[] board, int row, int col) {
		for (int i = 0; i < row; i++) {
			if (board[i] == col || board[i] == col - row + i || board[i] == col + row - i) {
				return false;
			}
		}

		return true;
	}


	public static long countSolutionsWithoutRecursion(int n) {
		int[] board = new int[n];
		int count = 0;
		int row = 0;
		int col = 0;

		while (true) {
			while (row < n && col < n){
				if (isSafe(board, row, col)) {
					board[row++] = col;
					col = 0;
				} else {
					col++;
				}
			}

			if (row >= n) {
				count++;
			}

			row--;
			if (row < 0) {
				return count;
			} else {
				col = board[row] + 1;
			}
		}
	}


	public static long countSolutionsWithThreads(int n) {
		long count = 0;
		int nThreads = (n + 1) / 2;
		ExecutorService pool = Executors.newFixedThreadPool(nThreads);
		List<Future<Long>> futureList = new ArrayList<>(nThreads);

		for (int i = 0; i < nThreads; i++) {
			int[] board = new int[n];
			board[0] = i;
			Callable<Long> c = new NQThread(board);
			futureList.add(pool.submit(c));
		}

		pool.shutdown();

		for (int i = 0; i < nThreads; i++) {
			try {
				long result = futureList.get(i).get();
				if (n % 2 == 1 && i == nThreads - 1) {
					count += result;
				} else {
					count += 2 * result;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return count;
	}


	private static class NQThread implements Callable<Long> {
		private int[] board;

		NQThread(int[] board) {
			this.board = board;
		}

		@Override
		public Long call() {
			return NQueens.placeQueenInRow(board, 1);
		}
	}


	private static void testAllApproaches(int n) {
		System.out.println(n + " Queens Compare");
		System.out.println("---------------------------------");

		for (int i = 0; i < 1; i++) {
			long begin = System.currentTimeMillis();

			long count = countSolutionsWithBitOp(n);

			long then = System.currentTimeMillis();

			System.out.format("Time: %5dms, Solutions: %7d\n",
				then - begin, count);
		}

		System.out.println("---------------------------------");
	}


	private static void testOneApproach(int min, int max) {
		for (int n = min; n <= max; n++) {
			long then = System.currentTimeMillis();

			long count = countSolutions(n);

			long now = System.currentTimeMillis();

			System.out.format("%2d Queens, Time: %5dms, Solutions: %7d\n",
				n, now - then, count);
		}
	}
}



/*
https://oeis.org/A000170/list
n 		a(n)
1		1
2		0
3		0
4		2
5		10
6		4
7		40
8		92
9		352
10		724
11		2680
12		14200
13		73712
14		365596
15		2279184
16		14772512
17		95815104
18		666090624
19		4968057848
20		39029188884
21		314666222712
22		2691008701644
23		24233937684440
24		227514171973736
25		2207893435808352
26		22317699616364044
27		234907967154122528
 */
