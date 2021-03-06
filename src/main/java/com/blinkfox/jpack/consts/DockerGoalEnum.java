package com.blinkfox.jpack.consts;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Docker 构建目标的枚举类.
 *
 * @author blinkfox on 2019-05-14.
 * @since v1.1.0
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DockerGoalEnum {

    /**
     * 导出镜像到本地为 '.tar' 文件.
     */
    SAVE("save"),

    /**
     * 推送镜像到远程仓库中.
     */
    PUSH("push");

    /**
     * 代码值.
     */
    private final String code;

    /**
     * 根据代码值的字符串找到对应的实例.
     *
     * @param codeStr 代码值字符串
     * @return DockerGoalEnum实例
     */
    public static DockerGoalEnum of(String codeStr) {
        for (DockerGoalEnum goalEnum : DockerGoalEnum.values()) {
            if (goalEnum.code.equalsIgnoreCase(codeStr)) {
                return goalEnum;
            }
        }
        return null;
    }

}
