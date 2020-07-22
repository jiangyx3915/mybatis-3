/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.parsing;

/**
 * 通用标记解析器
 * @author Clinton Begin
 */
public class GenericTokenParser {

  /**
   *  开始标记
   */
  private final String openToken;
  /**
   * 结束标记
   */
  private final String closeToken;
  /**
   * 标记处理器
   */
  private final TokenHandler handler;

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  public String parse(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    // search open token
    // 寻找开始标记的位置
    int start = text.indexOf(openToken);
    if (start == -1) {
      // 如果未找到起始标记则返回
      return text;
    }
    char[] src = text.toCharArray();
    // 起始查找位置
    int offset = 0;
    // 结果
    final StringBuilder builder = new StringBuilder();
    // 匹配到 openToken 和 closeToken之间的表达式
    StringBuilder expression = null;
    while (start > -1) {
      if (start > 0 && src[start - 1] == '\\') {
        // this open token is escaped. remove the backslash and continue.
        // 忽略转义字符，添加 offset 和 openToken 前一个转义字符之间的内容到 builder 中
        builder.append(src, offset, start - offset - 1).append(openToken);
        // 修改 offset 值
        offset = start + openToken.length();
      } else {
        // found open token. let's search close token.

        // 创建或重置 expression 对象
        if (expression == null) {
          expression = new StringBuilder();
        } else {
          expression.setLength(0);
        }
        // 添加 offset 和 openToken之间的内容至 builder 中
        builder.append(src, offset, start - offset);
        // 修改 offset 值
        offset = start + openToken.length();
        // 寻找 closeToken 的位置
        int end = text.indexOf(closeToken, offset);
        while (end > -1) {
          // 转义处理
          if (end > offset && src[end - 1] == '\\') {
            // this close token is escaped. remove the backslash and continue.
            // 忽略 closeToken 之前的一个转义字符 \
            // 添加 表达式内容 到 expression 即 openToken后到 closeToken前的转义字符之前的内容
            expression.append(src, offset, end - offset - 1).append(closeToken);
            // 更新 offset
            offset = end + closeToken.length();
            // 继续寻找 closeToken 的位置
            end = text.indexOf(closeToken, offset);
          } else {
            // 添加 表达式内容 到 expression 即 openToken后到 closeToken前的内容
            expression.append(src, offset, end - offset);
            break;
          }
        }
        if (end == -1) {
          // close token was not found.
          // 如果没找到 closeToken 则直接拼接 openToken 后的字符串到builder
          builder.append(src, start, src.length - start);
          // 直接设置 offset 为文本长度
          offset = src.length;
        } else {
          // 找到 closeToken ，将 expression 交给 handler 处理，并将返回结果到 builder中
          builder.append(handler.handleToken(expression.toString()));
          // 修改 offset
          offset = end + closeToken.length();
        }
      }
      // 继续，寻找下一个 openToken 的位置
      start = text.indexOf(openToken, offset);
    }
    // 如果 offset 位置 小于文本长度并且找不到 openToken 了，拼接剩下的字符串到 builder
    if (offset < src.length) {
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}
