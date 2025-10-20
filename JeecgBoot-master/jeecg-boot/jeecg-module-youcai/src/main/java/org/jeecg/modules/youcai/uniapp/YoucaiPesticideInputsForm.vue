<template>
    <view>
        <!--标题和返回-->
		<cu-custom :bgColor="NavBarColor" isBack :backRouterName="backRouteName">
			<block slot="backText">返回</block>
			<block slot="content">农药投入表</block>
		</cu-custom>
		 <!--表单区域-->
		<view>
			<form>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">生产计划ID：</text></view>
                  <input type="number" placeholder="请输入生产计划ID" v-model="model.planId"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">农药名称：</text></view>
                  <input  placeholder="请输入农药名称" v-model="model.pesticideName"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">农药类型：</text></view>
                  <input  placeholder="请输入农药类型" v-model="model.pesticideType"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">防治对象：</text></view>
                  <input  placeholder="请输入防治对象" v-model="model.targetPest"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">使用量(公斤/升)：</text></view>
                  <input type="number" placeholder="请输入使用量(公斤/升)" v-model="model.amount"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">成本(元)：</text></view>
                  <input type="number" placeholder="请输入成本(元)" v-model="model.cost"/>
                </view>
              </view>
              <my-date label="施药日期：" fields="day" v-model="model.applicationDate" placeholder="请输入施药日期"></my-date>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">施药方式：</text></view>
                  <input  placeholder="请输入施药方式" v-model="model.applicationMethod"/>
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
        name: "YoucaiPesticideInputsForm",
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
                  queryById: "/youcai/youcaiPesticideInputs/queryById",
                  add: "/youcai/youcaiPesticideInputs/add",
                  edit: "/youcai/youcaiPesticideInputs/edit",
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
