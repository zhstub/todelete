
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

		testAllSolutions(8);
		testOneSolutions(8, 14);
	}


	public static long getAllSolutionsWithBitOp(int num) {
		mask = (1 << num) - 1;

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


	public static long getAllSolutionsWithBigInteger(int num) {
		maskBigInteger = BigInteger.ONE.shiftLeft(num).subtract(BigInteger.ONE);

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


	public static long getAllSolutions(int num) {
		int[] chess = new int[num];

		return placeQueenAtRow(chess, 0);
	}


	public static long getAllSolutionsWithThreads(int num) {
		long count = 0;
		int nThreads = (num + 1) / 2;
		ExecutorService pool = Executors.newFixedThreadPool(nThreads);
		List<Future<Long>> futureList = new ArrayList<>(nThreads);

		for (int i = 0; i < nThreads; i++) {
			int[] chess = new int[num];
			chess[0] = i;
			Callable<Long> c = new NQThread(chess);
			futureList.add(pool.submit(c));
		}

		pool.shutdown();

		for (int i = 0; i < nThreads; i++) {
			try {
				long result = futureList.get(i).get();
				if (num % 2 == 1 && i == nThreads - 1) {
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


	// chess[n]=x表示第n行第x列有一个皇后
	private static long placeQueenAtRow(int[] chess, int row) {
		if (row == chess.length) {
			return 1;
		}

		long count = 0;

		if (row == 0) { // First queen
			// 棋盘对称，只需计算一半
			for (int i = 0; i < (chess.length + 1) / 2; i++) {
				chess[row] = i;
				long result = placeQueenAtRow(chess, row + 1);
				if (chess.length % 2 == 1 && i == chess.length / 2) {
					count += result;
				} else {
					count += 2 * result;
				}
			}
		} else {
			for (int i = 0; i < chess.length; i++) {
				if (isSafe(chess, row, i)) {
					chess[row] = i;
					count += placeQueenAtRow(chess, row + 1);
				}
			}
		}

		return count;
	}


	private static boolean isSafe(int[] chess, int row, int col) {
		for (int i = 0; i < row; i++) {
			if (chess[i] == col || chess[i] == col - row + i || chess[i] == col + row - i) {
				return false;
			}
		}

		return true;
	}


	private static class NQThread implements Callable<Long> {
		private int[] chess;

		NQThread(int[] chess) {
			this.chess = chess;
		}

		@Override
		public Long call() {
			return NQueens.placeQueenAtRow(chess, 1);
		}
	}


	private static void testAllSolutions(int num) {
		System.out.println(num + " Queens Compare");
		System.out.println("---------------------------------");

		for (int i = 0; i < 1; i++) {
			long begin = System.currentTimeMillis();

			long count = getAllSolutionsWithBitOp(num);

			long then = System.currentTimeMillis();

			System.out.format("Time: %5dms, Solutions: %7d\n",
				then - begin, count);
		}

		System.out.println("---------------------------------");
	}


	private static void testOneSolutions(int min, int max) {
		for (int num = min; num <= max; num++) {
			long then = System.currentTimeMillis();

			long count = getAllSolutionsWithBitOp(num);

			long now = System.currentTimeMillis();

			System.out.format("%2d Queens, Time: %5dms, Solutions: %7d\n",
				num, now - then, count);
		}
	}
}



/*
1       1
2       0
3       0
4       2
5       10
6       4
7       40
8       92
9       352
10      724
11      2680
12      14200
13      73712
14      365596
15      2279184
16      14772512
17      95815104
18      666090624
19      4968057848
20      39029188884
21      314666222712
22      2691008701644
23      24233937684440
24      227514171973736
25      2207893435808352
 */