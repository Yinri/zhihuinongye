<template>
    <view>
        <!--标题和返回-->
		<cu-custom :bgColor="NavBarColor" isBack :backRouterName="backRouteName">
			<block slot="backText">返回</block>
			<block slot="content">灌溉监控表</block>
		</cu-custom>
		 <!--表单区域-->
		<view>
			<form>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">地块ID：</text></view>
                  <input type="number" placeholder="请输入地块ID" v-model="model.plotId"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">生产计划ID：</text></view>
                  <input type="number" placeholder="请输入生产计划ID" v-model="model.planId"/>
                </view>
              </view>
              <my-date label="灌溉日期：" fields="day" v-model="model.irrigationDate" placeholder="请输入灌溉日期"></my-date>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">灌溉方式：</text></view>
                  <input  placeholder="请输入灌溉方式" v-model="model.irrigationMethod"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">灌溉量(立方米)：</text></view>
                  <input type="number" placeholder="请输入灌溉量(立方米)" v-model="model.waterAmount"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">灌溉时长(分钟)：</text></view>
                  <input type="number" placeholder="请输入灌溉时长(分钟)" v-model="model.irrigationDuration"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">删除标志（0-正常，1-删除）：</text></view>
                  <input type="number" placeholder="请输入删除标志（0-正常，1-删除）" v-model="model.delFlag"/>
                </view>
              </view>
				<view class="padding">
					<button class="cu-btn block bg-blue margin-tb-sm lg" @click="onSubmit">
						<text v-if="loading" class="cuIcon-loading2 cuIconfont-spin"></text>提交
					</button>
				</view>
			</form>
		</view>
    </view>
</template>

<script>
    import myDate from '@/components/my-componets/my-date.vue'

    export default {
        name: "YoucaiIrrigationMonitoringForm",
        components:{ myDate },
        props:{
          formData:{
              type:Object,
              default:()=>{},
              required:false
          }
        },
        data(){
            return {
				CustomBar: this.CustomBar,
				NavBarColor: this.NavBarColor,
				loading:false,
                model: {},
                backRouteName:'index',
                url: {
                  queryById: "/youcai/youcaiIrrigationMonitoring/queryById",
                  add: "/youcai/youcaiIrrigationMonitoring/add",
                  edit: "/youcai/youcaiIrrigationMonitoring/edit",
                },
            }
        },
        created(){
             this.initFormData();
        },
        methods:{
           initFormData(){
               if(this.formData){
                    let dataId = this.formData.dataId;
                    this.$http.get(this.url.queryById,{params:{id:dataId}}).then((res)=>{
                        if(res.data.success){
                            console.log("表单数据",res);
                            this.model = res.data.result;
                        }
                    })
                }
            },
            onSubmit() {
                let myForm = {...this.model};
                this.loading = true;
                let url = myForm.id?this.url.edit:this.url.add;
				this.$http.post(url,myForm).then(res=>{
				   console.log("res",res)
				   this.loading = false
				   this.$Router.push({name:this.backRouteName})
				}).catch(()=>{
					this.loading = false
				});
            }
        }
    }
</script>
