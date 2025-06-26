package org.example.dto.quark;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 夸克网盘保存文件请求DTO
 */
public class QuarkSaveRequest {

    @JsonProperty("fid_list")
    private List<String> fidList;

    @JsonProperty("fid_token_list")
    private List<String> fidTokenList;

    @JsonProperty("to_pdir_fid")
    private String toPdirFid;

    @JsonProperty("pwd_id")
    private String pwdId;

    @JsonProperty("stoken")
    private String stoken;

    @JsonProperty("pdir_fid")
    private String pdirFid;

    @JsonProperty("pdir_save_all")
    private Boolean pdirSaveAll;

    @JsonProperty("exclude_fids")
    private List<String> excludeFids;

    @JsonProperty("scene")
    private String scene;

    // Constructors
    public QuarkSaveRequest() {}

    public QuarkSaveRequest(List<String> fidList, List<String> fidTokenList,
                           String toPdirFid, String pwdId, String stoken,
                           String pdirFid, Boolean pdirSaveAll, List<String> excludeFids, String scene) {
        this.fidList = fidList;
        this.fidTokenList = fidTokenList;
        this.toPdirFid = toPdirFid;
        this.pwdId = pwdId;
        this.stoken = stoken;
        this.pdirFid = pdirFid;
        this.pdirSaveAll = pdirSaveAll;
        this.excludeFids = excludeFids;
        this.scene = scene;
    }

    // Getters and Setters
    public List<String> getFidList() {
        return fidList;
    }

    public void setFidList(List<String> fidList) {
        this.fidList = fidList;
    }

    public List<String> getFidTokenList() {
        return fidTokenList;
    }

    public void setFidTokenList(List<String> fidTokenList) {
        this.fidTokenList = fidTokenList;
    }

    public String getToPdirFid() {
        return toPdirFid;
    }

    public void setToPdirFid(String toPdirFid) {
        this.toPdirFid = toPdirFid;
    }

    public String getPwdId() {
        return pwdId;
    }

    public void setPwdId(String pwdId) {
        this.pwdId = pwdId;
    }

    public String getStoken() {
        return stoken;
    }

    public void setStoken(String stoken) {
        this.stoken = stoken;
    }

    public String getPdirFid() {
        return pdirFid;
    }

    public void setPdirFid(String pdirFid) {
        this.pdirFid = pdirFid;
    }

    public Boolean getPdirSaveAll() {
        return pdirSaveAll;
    }

    public void setPdirSaveAll(Boolean pdirSaveAll) {
        this.pdirSaveAll = pdirSaveAll;
    }

    public List<String> getExcludeFids() {
        return excludeFids;
    }

    public void setExcludeFids(List<String> excludeFids) {
        this.excludeFids = excludeFids;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    @Override
    public String toString() {
        return "QuarkSaveRequest{" +
                "fidList=" + fidList +
                ", fidTokenList=" + fidTokenList +
                ", toPdirFid='" + toPdirFid + '\'' +
                ", pwdId='" + pwdId + '\'' +
                ", stoken='" + stoken + '\'' +
                ", pdirFid='" + pdirFid + '\'' +
                ", pdirSaveAll=" + pdirSaveAll +
                ", excludeFids=" + excludeFids +
                ", scene='" + scene + '\'' +
                '}';
    }
}
