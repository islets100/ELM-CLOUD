package team.tjusw.elm.dto;

public class BirthdayModificationCheckDto {
	private boolean canModify;
	private String reason;

	public BirthdayModificationCheckDto() {
	}

	public BirthdayModificationCheckDto(boolean canModify, String reason) {
		this.canModify = canModify;
		this.reason = reason;
	}

	public boolean isCanModify() {
		return canModify;
	}

	public void setCanModify(boolean canModify) {
		this.canModify = canModify;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}

