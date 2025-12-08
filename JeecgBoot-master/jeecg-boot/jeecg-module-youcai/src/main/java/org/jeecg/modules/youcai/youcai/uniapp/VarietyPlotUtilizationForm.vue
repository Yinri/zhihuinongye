<template>
    <view>
        <!--标题和返回-->
		<cu-custom :bgColor="NavBarColor" isBack :backRouterName="backRouteName">
			<block slot="backText">返回</block>
			<block slot="content">variety_plot_utilization</block>
		</cu-custom>
		 <!--表单区域-->
		<view>
			<form>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">品种ID（关联作物品种表主键）：</text></view>
                  <input  placeholder="请输入品种ID（关联作物品种表主键）" v-model="model.varietyId"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">地块ID（关联地块表主键）：</text></view>
                  <input  placeholder="请输入地块ID（关联地块表主键）" v-model="model.plotId"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">肥料ID（关联肥料表主键）：</text></view>
                  <input  placeholder="请输入肥料ID（关联肥料表主键）" v-model="model.fertilizerId"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">肥料类型（冗余字段：如尿素、磷酸二铵）：</text></view>
                  <input  placeholder="请输入肥料类型（冗余字段：如尿素、磷酸二铵）" v-model="model.fertilizerType"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">氮肥利用率(%)：</text></view>
                  <input type="number" placeholder="请输入氮肥利用率(%)" v-model="model.nRate"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">磷肥利用率(%)：</text></view>
                  <input type="number" placeholder="请输入磷肥利用率(%)" v-model="model.pRate"/>
                </view>
              </view>
              <view class="cu-form-group">
                <view class="flex align-center">
                  <view class="title"><text space="ensp">钾肥利用率(%)：</text></view>
                  <input type="number" placeholder="请输入钾肥利用率(%)" v-model="model.kRate"/>
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
        name: "VarietyPlotUtilizationForm",
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
                  queryById: "/youcai/varietyPlotUtilization/queryById",
                  add: "/youcai/varietyPlotUtilization/add",
                  edit: "/youcai/varietyPlotUtilization/edit",
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
