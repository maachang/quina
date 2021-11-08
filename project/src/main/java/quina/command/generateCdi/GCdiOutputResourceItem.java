package quina.command.generateCdi;

import java.util.List;

import quina.nativeimage.ResourceItem;

/**
 * ResourceItemに検出した.propertiesファイルを出力.
 */
public class GCdiOutputResourceItem {
	private GCdiOutputResourceItem() {}
	
	/**
	 * ResourceItemに検出した.propertiesファイルを出力.
	 * @param params GCdiParamsを設定します.
	 * @return int 登録数が返却されます.
	 */
	public static final int outputResourceItem(GCdiParams params) {
		if(!params.resourceItemFlag) {
			return 0;
		}
		// ResourceItemに追加.
		String name;
		ResourceItem ri = ResourceItem.get();
		final List<String> list = params.resList;
		final int len = list.size();
		for(int i = 0; i < len; i ++) {
			name = list.get(i);
			ri.addBundleItem(name);
		}
		// 登録した場合、情報をクリアする.
		list.clear();
		return len;
	}
}
