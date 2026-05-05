<template>
	<div class="reward-rule-management">
		<!-- 顶部导航 -->
		<nav class="top-nav">
			<button @click="$router.go(-1)" class="back-btn">← 返回</button>
			<h1>奖励规则管理</h1>
		</nav>

		<!-- 操作区 -->
		<div class="action-bar">
			<button @click="showCreateModal('RECHARGE')" class="add-btn">
				+ 新增充值奖励
			</button>
			<button @click="showCreateModal('WITHDRAW')" class="add-btn withdraw">
				+ 新增提现手续费
			</button>
		</div>

		<!-- 充值奖励规则列表 -->
		<div class="rules-section">
			<h2 class="section-title">充值奖励规则</h2>
			<div class="rules-list">
				<div v-if="rechargeRules.length === 0" class="empty-state">
					暂无充值奖励规则
				</div>
				<div v-for="rule in rechargeRules" :key="rule.id" class="rule-card">
					<div class="rule-header">
						<h3>{{ rule.ruleName }}</h3>
						<div class="rule-status">
							<span :class="['status-badge', rule.enabled ? 'enabled' : 'disabled']">
								{{ rule.enabled ? '已启用' : '已禁用' }}
							</span>
						</div>
					</div>
					<div class="rule-content">
						<div class="rule-item">
							<span class="label">满额金额：</span>
							<span class="value">¥{{ rule.thresholdAmount }}</span>
						</div>
						<div class="rule-item">
							<span class="label">奖励金额：</span>
							<span class="value highlight">¥{{ rule.rewardAmount }}</span>
						</div>
						<div class="rule-item">
							<span class="label">优先级：</span>
							<span class="value">{{ rule.priority }}</span>
						</div>
					</div>
					<div class="rule-actions">
						<button @click="editRule(rule)" class="edit-btn">编辑</button>
						<button v-if="rule.enabled" @click="toggleRule(rule.id, false)" class="disable-btn">禁用</button>
						<button v-else @click="toggleRule(rule.id, true)" class="enable-btn">启用</button>
						<button @click="deleteRule(rule.id)" class="delete-btn">删除</button>
					</div>
				</div>
			</div>
		</div>

		<!-- 提现手续费规则列表 -->
		<div class="rules-section">
			<h2 class="section-title">提现手续费规则</h2>
			<div class="rules-list">
				<div v-if="withdrawRules.length === 0" class="empty-state">
					暂无提现手续费规则
				</div>
				<div v-for="rule in withdrawRules" :key="rule.id" class="rule-card">
					<div class="rule-header">
						<h3>{{ rule.ruleName }}</h3>
						<div class="rule-status">
							<span :class="['status-badge', rule.enabled ? 'enabled' : 'disabled']">
								{{ rule.enabled ? '已启用' : '已禁用' }}
							</span>
						</div>
					</div>
					<div class="rule-content">
						<div class="rule-item" v-if="rule.feeRate">
							<span class="label">手续费率：</span>
							<span class="value">{{ (rule.feeRate * 100).toFixed(2) }}%</span>
						</div>
						<div class="rule-item" v-if="rule.fixedFee">
							<span class="label">固定手续费：</span>
							<span class="value">¥{{ rule.fixedFee }}</span>
						</div>
						<div class="rule-item">
							<span class="label">优先级：</span>
							<span class="value">{{ rule.priority }}</span>
						</div>
					</div>
					<div class="rule-actions">
						<button @click="editRule(rule)" class="edit-btn">编辑</button>
						<button v-if="rule.enabled" @click="toggleRule(rule.id, false)" class="disable-btn">禁用</button>
						<button v-else @click="toggleRule(rule.id, true)" class="enable-btn">启用</button>
						<button @click="deleteRule(rule.id)" class="delete-btn">删除</button>
					</div>
				</div>
			</div>
		</div>

		<!-- 创建/编辑规则弹窗 -->
		<div class="modal-backdrop" v-if="showModal" @click.self="closeModal">
			<div class="modal">
				<div class="modal-header">
					<h3>{{ isEdit ? '编辑规则' : '创建规则' }}</h3>
					<button @click="closeModal" class="close-btn">×</button>
				</div>
				<div class="modal-body">
					<div class="form-group">
						<label>规则名称 <span class="required">*</span></label>
						<input v-model="formData.ruleName" type="text" placeholder="例如：充值满1000送100">
					</div>

					<!-- 充值规则表单 -->
					<template v-if="formData.ruleType === 'RECHARGE'">
						<div class="form-group">
							<label>满额金额 <span class="required">*</span></label>
							<input v-model.number="formData.thresholdAmount" type="number" placeholder="例如：1000">
						</div>
						<div class="form-group">
							<label>奖励金额 <span class="required">*</span></label>
							<input v-model.number="formData.rewardAmount" type="number" placeholder="例如：100">
						</div>
					</template>

					<!-- 提现规则表单 -->
					<template v-if="formData.ruleType === 'WITHDRAW'">
						<div class="form-group">
							<label>手续费率（%）</label>
							<input v-model.number="formData.feeRatePercent" type="number" step="0.01" placeholder="例如：10 表示10%">
							<span class="hint">留空则不收取比例手续费</span>
						</div>
						<div class="form-group">
							<label>固定手续费（元）</label>
							<input v-model.number="formData.fixedFee" type="number" step="0.01" placeholder="例如：5">
							<span class="hint">留空则不收取固定手续费</span>
						</div>
					</template>

					<div class="form-group">
						<label>优先级</label>
						<input v-model.number="formData.priority" type="number" placeholder="数字越大优先级越高">
						<span class="hint">当有多个规则时，优先使用优先级高的规则</span>
					</div>
				</div>
				<div class="modal-footer">
					<button @click="closeModal" class="cancel-btn">取消</button>
					<button @click="submitForm" class="confirm-btn">{{ isEdit ? '保存' : '创建' }}</button>
				</div>
			</div>
		</div>
	</div>
</template>

<script>
import axios from 'axios'

export default {
	name: 'RewardRuleManagement',
	data() {
		return {
			rechargeRules: [],
			withdrawRules: [],
			showModal: false,
			isEdit: false,
			formData: {
				id: null,
				ruleType: 'RECHARGE',
				ruleName: '',
				thresholdAmount: null,
				rewardAmount: null,
				feeRatePercent: null,
				fixedFee: null,
				priority: 0
			}
		}
	},
	created() {
		this.loadRules()
	},
	methods: {
		async loadRules() {
			try {
				const response = await axios.get('/api/reward-rules')
				if (response.data.success) {
					const rules = response.data.data
					this.rechargeRules = rules.filter(r => r.ruleType === 'RECHARGE')
					this.withdrawRules = rules.filter(r => r.ruleType === 'WITHDRAW')
				}
			} catch (err) {
				console.error('加载规则失败', err)
				alert('加载规则失败：' + (err.response?.data?.message || err.message))
			}
		},

		showCreateModal(ruleType) {
			this.isEdit = false
			this.formData = {
				id: null,
				ruleType: ruleType,
				ruleName: '',
				thresholdAmount: null,
				rewardAmount: null,
				feeRatePercent: null,
				fixedFee: null,
				priority: 0
			}
			this.showModal = true
		},

		editRule(rule) {
			this.isEdit = true
			this.formData = {
				id: rule.id,
				ruleType: rule.ruleType,
				ruleName: rule.ruleName,
				thresholdAmount: rule.thresholdAmount,
				rewardAmount: rule.rewardAmount,
				feeRatePercent: rule.feeRate ? rule.feeRate * 100 : null,
				fixedFee: rule.fixedFee,
				priority: rule.priority
			}
			this.showModal = true
		},

		closeModal() {
			this.showModal = false
		},

		async submitForm() {
			// 验证
			if (!this.formData.ruleName) {
				alert('请输入规则名称')
				return
			}

			if (this.formData.ruleType === 'RECHARGE') {
				if (!this.formData.thresholdAmount || !this.formData.rewardAmount) {
					alert('请填写满额金额和奖励金额')
					return
				}
			} else if (this.formData.ruleType === 'WITHDRAW') {
				if (!this.formData.feeRatePercent && !this.formData.fixedFee) {
					alert('请至少填写手续费率或固定手续费')
					return
				}
			}

			// 构建请求数据
			const data = {
				ruleType: this.formData.ruleType,
				ruleName: this.formData.ruleName,
				thresholdAmount: this.formData.thresholdAmount,
				rewardAmount: this.formData.rewardAmount,
				feeRate: this.formData.feeRatePercent ? this.formData.feeRatePercent / 100 : null,
				fixedFee: this.formData.fixedFee,
				priority: this.formData.priority || 0
			}

			try {
				if (this.isEdit) {
					// 更新
					await axios.put(`/api/reward-rules/${this.formData.id}`, data)
					alert('更新成功')
				} else {
					// 创建
					await axios.post('/api/reward-rules', data)
					alert('创建成功')
				}
				this.closeModal()
				this.loadRules()
			} catch (err) {
				console.error('操作失败', err)
				alert('操作失败：' + (err.response?.data?.message || err.message))
			}
		},

		async toggleRule(id, enable) {
			try {
				const url = `/api/reward-rules/${id}/${enable ? 'enable' : 'disable'}`
				await axios.put(url)
				alert(enable ? '启用成功' : '禁用成功')
				this.loadRules()
			} catch (err) {
				console.error('操作失败', err)
				alert('操作失败：' + (err.response?.data?.message || err.message))
			}
		},

		async deleteRule(id) {
			if (!confirm('确定要删除这条规则吗？')) {
				return
			}
			try {
				await axios.delete(`/api/reward-rules/${id}`)
				alert('删除成功')
				this.loadRules()
			} catch (err) {
				console.error('删除失败', err)
				alert('删除失败：' + (err.response?.data?.message || err.message))
			}
		}
	}
}
</script>

<style scoped>
.reward-rule-management {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 20px;
}

.top-nav {
	position: sticky;
	top: 0;
	background-color: #3190e8;
	color: white;
	padding: 15px 20px;
	display: flex;
	align-items: center;
	gap: 20px;
	z-index: 100;
	box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.back-btn {
	background: none;
	border: none;
	color: white;
	font-size: 16px;
	cursor: pointer;
	padding: 5px 10px;
}

.top-nav h1 {
	font-size: 20px;
	margin: 0;
}

.action-bar {
	padding: 20px;
	display: flex;
	gap: 10px;
}

.add-btn {
	padding: 10px 20px;
	background-color: #3190e8;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 14px;
}

.add-btn.withdraw {
	background-color: #ff6b6b;
}

.add-btn:hover {
	opacity: 0.9;
}

.rules-section {
	margin: 20px;
}

.section-title {
	font-size: 18px;
	margin-bottom: 15px;
	color: #333;
	border-left: 4px solid #3190e8;
	padding-left: 10px;
}

.rules-list {
	display: grid;
	gap: 15px;
}

.empty-state {
	text-align: center;
	padding: 40px;
	color: #999;
	background: white;
	border-radius: 8px;
}

.rule-card {
	background: white;
	border-radius: 8px;
	padding: 20px;
	box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.rule-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 15px;
	padding-bottom: 15px;
	border-bottom: 1px solid #eee;
}

.rule-header h3 {
	margin: 0;
	font-size: 16px;
	color: #333;
}

.status-badge {
	padding: 4px 12px;
	border-radius: 12px;
	font-size: 12px;
	font-weight: bold;
}

.status-badge.enabled {
	background-color: #d4edda;
	color: #155724;
}

.status-badge.disabled {
	background-color: #f8d7da;
	color: #721c24;
}

.rule-content {
	display: grid;
	gap: 10px;
	margin-bottom: 15px;
}

.rule-item {
	display: flex;
	justify-content: space-between;
	font-size: 14px;
}

.rule-item .label {
	color: #666;
}

.rule-item .value {
	font-weight: bold;
	color: #333;
}

.rule-item .value.highlight {
	color: #3190e8;
}

.rule-actions {
	display: flex;
	gap: 10px;
}

.rule-actions button {
	padding: 6px 12px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 13px;
}

.edit-btn {
	background-color: #ffc107;
	color: white;
}

.enable-btn {
	background-color: #28a745;
	color: white;
}

.disable-btn {
	background-color: #6c757d;
	color: white;
}

.delete-btn {
	background-color: #dc3545;
	color: white;
}

/* 弹窗样式 */
.modal-backdrop {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0,0,0,0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
}

.modal {
	background: white;
	border-radius: 8px;
	width: 90%;
	max-width: 500px;
	max-height: 80vh;
	overflow-y: auto;
}

.modal-header {
	padding: 20px;
	border-bottom: 1px solid #eee;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.modal-header h3 {
	margin: 0;
	font-size: 18px;
}

.close-btn {
	background: none;
	border: none;
	font-size: 24px;
	cursor: pointer;
	color: #999;
}

.modal-body {
	padding: 20px;
}

.form-group {
	margin-bottom: 20px;
}

.form-group label {
	display: block;
	margin-bottom: 8px;
	font-weight: bold;
	color: #333;
}

.required {
	color: #dc3545;
}

.form-group input {
	width: 100%;
	padding: 10px;
	border: 1px solid #ddd;
	border-radius: 4px;
	font-size: 14px;
	box-sizing: border-box;
}

.hint {
	display: block;
	margin-top: 5px;
	font-size: 12px;
	color: #999;
}

.modal-footer {
	padding: 20px;
	border-top: 1px solid #eee;
	display: flex;
	justify-content: flex-end;
	gap: 10px;
}

.cancel-btn, .confirm-btn {
	padding: 10px 20px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 14px;
}

.cancel-btn {
	background-color: #6c757d;
	color: white;
}

.confirm-btn {
	background-color: #3190e8;
	color: white;
}
</style>
