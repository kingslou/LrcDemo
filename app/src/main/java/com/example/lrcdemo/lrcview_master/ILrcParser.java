package com.example.lrcdemo.lrcview_master;

import java.util.List;

/**
 * 歌词解析器
 * @author Ligang  2014/8/19
 *
 */
public interface ILrcParser {

	List<LrcRow> getLrcRows(String str);
}
