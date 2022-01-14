package quina.compile.tool;

import java.util.List;

import quina.nativeimage.ResourceItem;

/**
 * graalvmのNativeImageに認識させるjar内のpropertiesファイル名を
 * 反映する.
 */
public class GraalvmAppendResourceItem {
	private GraalvmAppendResourceItem() {}
	
	/**
	 * graalvmのNativeImageに認識させるjar内の
	 * propertiesファイル名を反映.
	 * @param params QuinaCTParamsを設定します.
	 * @return int 登録数が返却されます.
	 */
	public static final int apped(QuinaCTParams params) {
		if(!params.registerResourceItemFlag) {
			return 0;
		}
		// ResourceItemに追加.
		String name;
		ResourceItem ri = ResourceItem.get();
		final List<String> list = params.regResourceList;
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
