package team163.utils;

public class Test {

	public static void main(String[] args) {
		Point myHQ = new Point(10,10);
		Point enemyHQ = new Point(110,110);
		Path p = new Path(myHQ, enemyHQ);
		System.out.println(p.offsetMyHQ);
		System.out.println(p.offsetEnemyHQ);
		Point obs = new Point(50,50);
		Point obs_off = p.offsetPoint(obs);
		System.out.println(obs_off);
		System.out.println(p.unOffsetPoint(obs_off));
	}
}
