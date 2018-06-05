
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;


public class NQueens {
	private static long mask;
	private static BigInteger maskBigInteger;

	public static void main(String[] args) {
		int[] test = {1, 3, 5, 7, 9, 0, 2, 4, 6, 8};

		for (int i = 0; i < 10; i++) {
			int board[] = getExplicitSolution(i);
			System.out.println(CheckSolutionIfValid(board));
			System.out.println(Arrays.toString(board));
			printSolution(board);
		}

		testAllApproaches(8);
		testOneApproach(8, 13);
	}


	public static long countSolutionsWithBitOp(int n) {
		mask = (1 << n) - 1;

		return checkBits(0, 0, 0);
	}


	private static long checkBits(long row, long ld, long rd) {
		if (row == mask) {
			return 1;
		}

		long count = 0;
		long pos = ~(row | ld | rd) & mask;
		while (pos != 0) {
			long bit = pos & -pos;
			pos = pos & ~bit;
			count += checkBits(row | bit, (ld | bit) << 1, (rd | bit) >>> 1);
		}

		return count;
	}


	public static long countSolutionsWithBigInteger(int n) {
		maskBigInteger = BigInteger.ONE.shiftLeft(n).subtract(BigInteger.ONE);

		return checkBigInteger(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
	}


	private static long checkBigInteger(BigInteger row, BigInteger ld, BigInteger rd) {
		if (row.equals(maskBigInteger)) {
			return 1;
		}

		long count = 0;

		BigInteger pos = row.or(ld).or(rd).not().and(maskBigInteger);
		while (!pos.equals(BigInteger.ZERO)) {
			BigInteger bit = pos.and(pos.negate());
			pos = pos.and(bit.not());
			count += checkBigInteger(row.or(bit), ld.or(bit).shiftLeft(1), rd.or(bit).shiftRight(1));
		}

		return count;
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
			while (row < n && col < n) {
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


	// explicit solutions exist for all n ≥ 4
	public static int[] getExplicitSolution(int n) {
		if (n == 1) {
			return new int[]{0};
		} else if (n < 4) {
			return null;
		}

		int[] board = new int[n];
		int even = (n % 2 == 0) ? n : n - 1;

		if ((even - 2) % 6 != 0) {
			for (int i = 0; i < even / 2; i++) {
				board[i] = 2 * i + 1;
				board[even / 2 + i] = 2 * i;
			}
		} else if (even % 6 != 0) {
			for (int i = 0; i < even / 2; i++) {
				board[i] = (2 * i + even / 2 - 1) % even;
				board[even - i - 1] = even - (2 * i + even / 2 - 1) % even - 1;
			}
		}

		if (even != n) {
			board[n - 1] = n - 1;
		}

		return board;
	}


	public static int[] FindSolution() {

		return null;
	}


	public static boolean CheckSolutionIfValid(int[] board) {
		if (board == null) {
			return false;
		}

		int n = board.length;
		BigInteger mask = BigInteger.ONE.shiftLeft(n).subtract(BigInteger.ONE);
		BigInteger row = BigInteger.ZERO;
		BigInteger ld = BigInteger.ZERO;
		BigInteger rd = BigInteger.ZERO;

		for (int i = 0; i < n; i++) {
			BigInteger pos = row.or(ld).or(rd).not().and(mask);

			if (pos.testBit(board[i])) {
				row = row.setBit(board[i]);
				ld = ld.setBit(board[i]).shiftLeft(1);
				rd = rd.setBit(board[i]).shiftRight(1);
			} else {
				return false;
			}
		}

		return true;
	}


	public static void printSolution(int[] board) {
		if (board == null) {
			return;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board.length; j++) {
				if (j == board[i]) {
					sb.append("O ");
				} else {
					sb.append("* ");
				}
			}

			sb.append(" (");
			sb.append(board[i]);
			sb.append(")\n");
		}

		System.out.println(sb);
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
